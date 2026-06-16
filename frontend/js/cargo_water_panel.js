const CargoWaterPanel = {
    container: null,
    configs: [],
    _throttleTimer: null,
    cargoNames: {
        'SILK': '丝绸',
        'SPICE': '香料',
        'JADE': '玉石',
        'TEA': '茶叶',
        'PORCELAIN': '瓷器',
        'HORSE': '马匹',
        'GOLD_SILVER': '金银',
        'GENERAL': '普通'
    },
    terrainNames: {
        'DESERT': '沙漠',
        'OASIS': '绿洲',
        'MOUNTAIN': '山地',
        'GRASSLAND': '草原',
        'PLATEAU': '高原',
        'GOBI': '戈壁'
    },

    init(containerId) {
        this.container = document.getElementById(containerId);
        if (!this.container) return;
        this.render();
        this.loadConfigs();
    },

    render() {
        this.container.innerHTML = `
            <div class="panel">
                <h2>🐪 载重与水源消耗优化</h2>
                <div class="form-group">
                    <label>货物类型</label>
                    <select class="form-control" id="cargoTypeSelect">
                        <option value="">加载中...</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>骆驼数量</label>
                    <input type="number" class="form-control" id="camelCount" value="10" min="1" max="500">
                </div>
                <div class="form-group">
                    <label>人员数量</label>
                    <input type="number" class="form-control" id="crewCount" value="5" min="1" max="200">
                </div>
                <div class="form-group">
                    <label>货物重量 (kg)</label>
                    <input type="number" class="form-control" id="cargoWeight" value="2000" min="0" max="100000">
                </div>
                <div class="form-group">
                    <label>地形类型</label>
                    <select class="form-control" id="terrainTypeSelect">
                        <option value="DESERT">沙漠</option>
                        <option value="OASIS">绿洲</option>
                        <option value="MOUNTAIN">山地</option>
                        <option value="GRASSLAND">草原</option>
                        <option value="PLATEAU">高原</option>
                        <option value="GOBI">戈壁</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>温度: <span id="tempLabel">25</span>°C</label>
                    <input type="range" class="form-control" id="temperatureSlider"
                        min="-20" max="50" value="25" step="1" style="width:100%;cursor:pointer;">
                </div>
                <button class="btn btn-primary" id="analyzeBtn">分析优化</button>
                <div id="cargoResult" style="margin-top:15px;"></div>
                <div id="waterChart" style="margin-top:15px;"></div>
            </div>
        `;
        this.bindEvents();
    },

    bindEvents() {
        document.getElementById('analyzeBtn').addEventListener('click', () => this.analyze());
        document.getElementById('temperatureSlider').addEventListener('input', (e) => {
            document.getElementById('tempLabel').textContent = e.target.value;
            this.scheduleAnalyze();
        });
        ['cargoTypeSelect', 'camelCount', 'crewCount', 'cargoWeight', 'terrainTypeSelect'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.addEventListener('change', () => this.scheduleAnalyze());
            if (el && el.type === 'number') el.addEventListener('input', () => this.scheduleAnalyze());
        });
    },

    scheduleAnalyze() {
        if (this._throttleTimer) clearTimeout(this._throttleTimer);
        this._throttleTimer = setTimeout(() => this.analyze(), 300);
    },

    async loadConfigs() {
        const select = document.getElementById('cargoTypeSelect');
        try {
            const data = await API.getCargoConfigs();
            this.configs = Array.isArray(data) ? data : [];
            const options = this.configs.map(c => {
                const code = c.code || c.type || c.id;
                const name = this.cargoNames[code] || c.name || code;
                return `<option value="${code}">${name}</option>`;
            }).join('');
            select.innerHTML = options || '<option value="">暂无数据</option>';
        } catch (error) {
            console.error('加载货物配置失败:', error);
            select.innerHTML = `
                <option value="SILK">丝绸</option>
                <option value="SPICE">香料</option>
                <option value="JADE">玉石</option>
                <option value="TEA">茶叶</option>
                <option value="PORCELAIN">瓷器</option>
                <option value="HORSE">马匹</option>
                <option value="GOLD_SILVER">金银</option>
                <option value="GENERAL">普通</option>
            `;
        }
    },

    getFormData() {
        return {
            cargoType: document.getElementById('cargoTypeSelect').value || 'GENERAL',
            camelCount: parseInt(document.getElementById('camelCount').value) || 0,
            crewCount: parseInt(document.getElementById('crewCount').value) || 0,
            cargoWeightKg: parseFloat(document.getElementById('cargoWeight').value) || 0,
            terrainType: document.getElementById('terrainTypeSelect').value || 'DESERT',
            temperatureC: parseInt(document.getElementById('temperatureSlider').value) || 25
        };
    },

    async analyze() {
        const div = document.getElementById('cargoResult');
        const data = this.getFormData();
        if (data.camelCount <= 0 || data.crewCount <= 0) return;
        div.innerHTML = '<div class="loading">加载中...</div>';
        try {
            const result = await API.optimizeCargoWater(data);
            this.renderResult(result, data);
            this.generateWaterSimulation(result, data);
        } catch (error) {
            console.error('载重分析失败:', error);
            this.renderFallbackResult(data);
        }
    },

    renderFallbackResult(data) {
        const camelCapacity = 200;
        const maxLoad = data.camelCount * camelCapacity;
        const dailyCamelWater = 40;
        const dailyCrewWater = 3;
        const tempFactor = data.temperatureC > 30 ? 1.5 : data.temperatureC < 0 ? 0.8 : 1;
        const dailyWater = (data.camelCount * dailyCamelWater + data.crewCount * dailyCrewWater) * tempFactor;
        const loadRatio = Math.min(100, (data.cargoWeightKg / maxLoad) * 100);

        this.renderResult({
            optimalLoadKg: maxLoad * 0.8,
            maxLoadKg: maxLoad,
            currentLoadKg: data.cargoWeightKg,
            loadRatio: loadRatio / 100,
            dailyWaterConsumptionLiters: dailyWater,
            efficiencyRatio: data.cargoWeightKg / Math.max(dailyWater, 1),
            recommendedWaterCapacityLiters: dailyWater * 15,
            suggestionLevel: loadRatio > 95 ? 'DANGER' : loadRatio > 80 ? 'WARNING' : 'SAFE',
            suggestions: [
                loadRatio > 95 ? '⚠️ 载重接近上限，请减少货物或增加骆驼' :
                loadRatio > 80 ? '⚡ 载重较高，注意骆驼体力消耗' : '✅ 载重合理',
                `建议携带 ${(dailyWater * 15).toFixed(0)} 升水（15天用量）`,
                data.temperatureC > 35 ? '🔥 高温天气请额外增加20%水储备' : ''
            ].filter(Boolean)
        }, data);
    },

    renderResult(result, data) {
        const div = document.getElementById('cargoResult');
        if (!result) {
            div.innerHTML = '<div class="empty-state">分析失败</div>';
            return;
        }
        const optimal = result.optimalLoadKg || 0;
        const max = result.maxLoadKg || 0;
        const current = result.currentLoadKg || data.cargoWeightKg || 0;
        const loadPct = Math.min(100, max > 0 ? (current / max) * 100 : 0);
        const optimalPct = Math.min(100, max > 0 ? (optimal / max) * 100 : 0);
        const dailyWater = result.dailyWaterConsumptionLiters || 0;
        const eff = result.efficiencyRatio || 0;
        const recWater = result.recommendedWaterCapacityLiters || 0;
        const level = result.suggestionLevel || 'SAFE';
        const levelColors = { SAFE: '#4ade80', WARNING: '#fbbf24', DANGER: '#ef4444' };
        const color = levelColors[level] || '#4ade80';

        div.innerHTML = `
            <div style="background:#0f172a;padding:12px;border-radius:6px;margin-bottom:10px;">
                <h4 style="color:#e94560;margin-bottom:10px;font-size:0.9rem;">📦 载重分析</h4>
                <div style="margin-bottom:12px;">
                    <div style="display:flex;justify-content:space-between;font-size:0.8rem;margin-bottom:4px;">
                        <span style="color:#aaa;">当前载重 / 最大载重</span>
                        <span style="color:#e0e0e0;">${current.toFixed(0)} / ${max.toFixed(0)} kg</span>
                    </div>
                    <div style="position:relative;height:22px;background:#2a2a4a;border-radius:11px;overflow:hidden;">
                        <div style="position:absolute;left:0;top:0;bottom:0;width:${optimalPct}%;background:rgba(74,222,128,0.3);border-right:2px dashed #4ade80;"></div>
                        <div style="position:absolute;left:0;top:0;bottom:0;width:${loadPct}%;background:${loadPct>90?'#ef4444':loadPct>75?'#fbbf24':'#4ade80'};transition:width 0.3s;"></div>
                        <span style="position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);font-size:0.75rem;color:#fff;font-weight:600;text-shadow:0 0 4px #000;">${loadPct.toFixed(0)}%</span>
                    </div>
                    <div style="display:flex;justify-content:space-between;font-size:0.7rem;margin-top:4px;">
                        <span style="color:#4ade80;">最优 ${optimal.toFixed(0)}kg</span>
                        <span style="color:#888;">地形: ${this.terrainNames[data.terrainType] || data.terrainType}</span>
                    </div>
                </div>
                <div class="stat-row" style="display:flex;justify-content:space-between;margin-bottom:4px;font-size:0.85rem;">
                    <span style="color:#aaa;">💧 每日水消耗:</span>
                    <span style="color:#60a5fa;font-weight:600;">${dailyWater.toFixed(1)} L</span>
                </div>
                <div class="stat-row" style="display:flex;justify-content:space-between;margin-bottom:4px;font-size:0.85rem;">
                    <span style="color:#aaa;">⚖️ 载重/水效率:</span>
                    <span style="color:#e0e0e0;font-weight:500;">${eff.toFixed(1)} kg/L</span>
                </div>
                <div class="stat-row" style="display:flex;justify-content:space-between;margin-bottom:8px;font-size:0.85rem;">
                    <span style="color:#aaa;">💦 建议水容量:</span>
                    <span style="color:#e94560;font-weight:600;">${recWater.toFixed(0)} L</span>
                </div>
                <div style="padding:8px;background:rgba(${level==='SAFE'?'74,222,128':level==='WARNING'?'251,191,36':'239,68,68'},0.1);border-left:3px solid ${color};border-radius:4px;">
                    <div style="font-weight:600;color:${color};margin-bottom:4px;">
                        ${level==='SAFE'?'✅ 状态安全':level==='WARNING'?'⚠️ 需要注意':'🚨 危险预警'}
                    </div>
                    <ul style="list-style:none;padding-left:0;margin:0;">
                        ${(result.suggestions || []).map(s => `<li style="padding:2px 0;color:#ccc;font-size:0.8rem;">${s}</li>`).join('')}
                    </ul>
                </div>
            </div>
        `;
    },

    generateWaterSimulation(result, data) {
        const days = 15;
        const dailyWater = result?.dailyWaterConsumptionLiters || 300;
        const startWater = result?.recommendedWaterCapacityLiters || dailyWater * 15;
        const consumptionData = [];
        let remaining = startWater;
        for (let i = 0; i < days; i++) {
            const variance = 0.9 + Math.random() * 0.2;
            const used = dailyWater * variance;
            remaining = Math.max(0, remaining - used);
            consumptionData.push({
                day: i + 1,
                used: used,
                remaining: remaining
            });
        }
        this.showWaterSimulationChart(days, consumptionData);
    },

    showWaterSimulationChart(days, consumptionData) {
        const div = document.getElementById('waterChart');
        if (!consumptionData || consumptionData.length === 0) {
            div.innerHTML = '';
            return;
        }
        const maxUsed = Math.max(...consumptionData.map(d => d.used));
        const startWater = consumptionData[0]?.remaining + consumptionData[0]?.used || 0;
        div.innerHTML = `
            <div style="background:#0f172a;padding:12px;border-radius:6px;">
                <h4 style="color:#e94560;margin-bottom:10px;font-size:0.9rem;">📊 ${days}天水消耗模拟</h4>
                <div style="display:flex;align-items:flex-end;gap:4px;height:120px;margin-bottom:8px;border-bottom:1px solid #2a2a4a;padding-bottom:4px;">
                    ${consumptionData.map(d => {
                        const h = maxUsed > 0 ? (d.used / maxUsed) * 100 : 0;
                        const color = d.remaining < startWater * 0.2 ? '#ef4444' : d.remaining < startWater * 0.5 ? '#fbbf24' : '#60a5fa';
                        return `
                            <div style="flex:1;display:flex;flex-direction:column;align-items:center;gap:2px;">
                                <div style="width:100%;height:${h}%;background:${color};border-radius:2px 2px 0 0;min-height:4px;"
                                    title="第${d.day}天: 消耗${d.used.toFixed(0)}L, 剩余${d.remaining.toFixed(0)}L"></div>
                                <span style="font-size:0.6rem;color:#666;">${d.day}</span>
                            </div>
                        `;
                    }).join('')}
                </div>
                <div style="display:flex;justify-content:space-between;font-size:0.75rem;">
                    <span style="color:#60a5fa;">▲ 每日用水量</span>
                    <span style="color:#888;">第1天 → 第${days}天</span>
                </div>
                <div style="margin-top:8px;padding:6px;background:#1a1a2e;border-radius:4px;font-size:0.8rem;">
                    <div style="display:flex;justify-content:space-between;">
                        <span style="color:#aaa;">起始水量:</span>
                        <span style="color:#e0e0e0;">${startWater.toFixed(0)} L</span>
                    </div>
                    <div style="display:flex;justify-content:space-between;">
                        <span style="color:#aaa;">第${days}天剩余:</span>
                        <span style="color:${consumptionData[days-1]?.remaining < startWater*0.2?'#ef4444':'#4ade80'};">${(consumptionData[days-1]?.remaining || 0).toFixed(0)} L</span>
                    </div>
                </div>
            </div>
        `;
    }
};

window.CargoWaterPanel = CargoWaterPanel;
