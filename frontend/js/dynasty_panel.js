const DynastyPanel = {
    container: null,
    dynasties: [],
    currentDynasty: null,
    dynastyColors: {
        'HAN': '#fbbf24',
        'TANG': '#ef4444',
        'SONG': '#3b82f6',
        'YUAN': '#a855f7',
        'MING': '#10b981'
    },
    dynastyNames: {
        'HAN': '汉代',
        'TANG': '唐代',
        'SONG': '宋代',
        'YUAN': '元代',
        'MING': '明代'
    },

    init(containerId) {
        this.container = document.getElementById(containerId);
        if (!this.container) return;
        this.render();
        this.loadDynasties();
    },

    render() {
        this.container.innerHTML = `
            <div class="panel">
                <h2>🏛️ 朝代丝绸之路变迁</h2>
                <div class="form-group">
                    <label>选择朝代</label>
                    <select class="form-control" id="dynastySelect">
                        <option value="">选择朝代查看路线</option>
                    </select>
                </div>
                <div style="display:flex;gap:8px;margin-bottom:12px;">
                    <button class="btn btn-primary" id="showDynastyBtn">显示路线</button>
                    <button class="btn btn-secondary" id="clearDynastyBtn">清除路线</button>
                </div>
                <div class="form-group">
                    <label>对比朝代 A</label>
                    <select class="form-control" id="compareSelectA">
                        <option value="">选择朝代 A</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>对比朝代 B</label>
                    <select class="form-control" id="compareSelectB">
                        <option value="">选择朝代 B</option>
                    </select>
                </div>
                <button class="btn btn-primary" id="compareBtn">对比朝代</button>
                <div id="dynastyStats" style="margin-top:15px;"></div>
                <div id="dynastyProgress" style="margin-top:15px;"></div>
                <div id="compareResult" style="margin-top:15px;"></div>
            </div>
        `;
        this.bindEvents();
    },

    bindEvents() {
        document.getElementById('showDynastyBtn').addEventListener('click', () => {
            const dynasty = document.getElementById('dynastySelect').value;
            if (dynasty) this.showDynastyRoutes(dynasty);
        });
        document.getElementById('clearDynastyBtn').addEventListener('click', () => {
            if (window.SilkRoadMap && typeof window.SilkRoadMap.clearDynastyRoutes === 'function') {
                window.SilkRoadMap.clearDynastyRoutes();
            }
        });
        document.getElementById('compareBtn').addEventListener('click', () => {
            const a = document.getElementById('compareSelectA').value;
            const b = document.getElementById('compareSelectB').value;
            if (a && b && a !== b) this.compareDynasties(a, b);
            else alert('请选择两个不同的朝代');
        });
    },

    async loadDynasties() {
        const statsDiv = document.getElementById('dynastyStats');
        statsDiv.innerHTML = '<div class="loading">加载中...</div>';
        try {
            const data = await API.getDynasties();
            this.dynasties = Array.isArray(data) ? data : [];
            this.renderDynastySelects();
            this.renderStatsCards();
            this.renderProgressBars();
        } catch (error) {
            console.error('加载朝代数据失败:', error);
            statsDiv.innerHTML = '<div class="empty-state">加载失败</div>';
        }
    },

    renderDynastySelects() {
        const options = this.dynasties.map(d => {
            const code = d.code || d.dynasty || d.id;
            const name = this.dynastyNames[code] || d.name || code;
            return `<option value="${code}">${name}</option>`;
        }).join('');
        const dynastySelect = document.getElementById('dynastySelect');
        const compareA = document.getElementById('compareSelectA');
        const compareB = document.getElementById('compareSelectB');
        if (dynastySelect) dynastySelect.innerHTML = '<option value="">选择朝代查看路线</option>' + options;
        if (compareA) compareA.innerHTML = '<option value="">选择朝代 A</option>' + options;
        if (compareB) compareB.innerHTML = '<option value="">选择朝代 B</option>' + options;
    },

    renderStatsCards() {
        const div = document.getElementById('dynastyStats');
        if (!this.dynasties || this.dynasties.length === 0) {
            div.innerHTML = '<div class="empty-state">暂无朝代数据</div>';
            return;
        }
        div.innerHTML = `
            <div class="weather-grid" style="grid-template-columns:1fr 1fr 1fr;">
                ${this.dynasties.slice(0, 6).map(d => {
                    const code = d.code || d.dynasty || d.id;
                    const color = this.dynastyColors[code] || '#e94560';
                    const name = this.dynastyNames[code] || d.name || code;
                    return `
                        <div style="background:#0f172a;padding:10px;border-radius:6px;border-left:3px solid ${color};">
                            <div style="font-size:0.8rem;color:#aaa;margin-bottom:4px;">${name}</div>
                            <div style="font-size:0.9rem;color:#e0e0e0;">
                                <div>📏 ${(d.totalDistanceKm || d.totalDistance || 0).toFixed(0)} km</div>
                                <div>🛤️ ${d.routeCount || d.routes || 0} 条路线</div>
                                <div>💰 贸易: ${((d.tradeScore || d.trade || 0) * 100).toFixed(0)}%</div>
                                <div>📜 文化: ${((d.cultureScore || d.culture || 0) * 100).toFixed(0)}%</div>
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>
        `;
    },

    renderProgressBars() {
        const div = document.getElementById('dynastyProgress');
        if (!this.dynasties || this.dynasties.length === 0) return;
        div.innerHTML = `
            <h3 style="font-size:0.9rem;color:#e94560;margin-bottom:10px;padding-bottom:6px;border-bottom:1px solid #2a2a4a;">朝代指标</h3>
            ${this.dynasties.map(d => {
                const code = d.code || d.dynasty || d.id;
                const color = this.dynastyColors[code] || '#e94560';
                const name = this.dynastyNames[code] || d.name || code;
                const political = (d.politicalStability || d.political || 0) * 100;
                const trade = (d.tradeVolume || d.tradeScore || d.trade || 0) * 100;
                const culture = (d.cultureExchange || d.cultureScore || d.culture || 0) * 100;
                return `
                    <div style="margin-bottom:12px;background:#0f172a;padding:10px;border-radius:6px;">
                        <div style="font-weight:600;color:${color};margin-bottom:8px;">${name}</div>
                        <div style="display:flex;gap:10px;justify-content:space-around;">
                            ${this.renderCircleProgress('政治', political, color)}
                            ${this.renderCircleProgress('贸易', trade, color)}
                            ${this.renderCircleProgress('文化', culture, color)}
                        </div>
                    </div>
                `;
            }).join('')}
        `;
    },

    renderCircleProgress(label, value, color) {
        const v = Math.min(100, Math.max(0, value));
        const angle = (v / 100) * 360;
        const radius = 28;
        const circumference = 2 * Math.PI * radius;
        const offset = circumference - (v / 100) * circumference;
        return `
            <div style="text-align:center;">
                <svg width="70" height="70" viewBox="0 0 80 80">
                    <circle cx="40" cy="40" r="${radius}" fill="none" stroke="#2a2a4a" stroke-width="6"/>
                    <circle cx="40" cy="40" r="${radius}" fill="none" stroke="${color}" stroke-width="6"
                        stroke-dasharray="${circumference}" stroke-dashoffset="${offset}"
                        stroke-linecap="round" transform="rotate(-90 40 40)"/>
                    <text x="40" y="44" text-anchor="middle" fill="${color}" font-size="13" font-weight="600">${v.toFixed(0)}%</text>
                </svg>
                <div style="font-size:0.7rem;color:#888;margin-top:2px;">${label}</div>
            </div>
        `;
    },

    async showDynastyRoutes(dynasty) {
        this.currentDynasty = dynasty;
        const color = this.dynastyColors[dynasty] || '#e94560';
        try {
            const routes = await API.getDynastyRoutes(dynasty);
            if (window.SilkRoadMap && typeof window.SilkRoadMap.showDynastyRoutes === 'function') {
                window.SilkRoadMap.showDynastyRoutes(routes, dynasty, color);
            }
        } catch (error) {
            console.error('加载朝代路线失败:', error);
        }
    },

    async compareDynasties(dynastyA, dynastyB) {
        const div = document.getElementById('compareResult');
        div.innerHTML = '<div class="loading">加载中...</div>';
        try {
            const result = await API.compareDynasties(dynastyA, dynastyB);
            if (!result) {
                div.innerHTML = '<div class="empty-state">暂无对比数据</div>';
                return;
            }
            const aName = this.dynastyNames[dynastyA] || dynastyA;
            const bName = this.dynastyNames[dynastyB] || dynastyB;
            const a = result.dynastyA || result[dynastyA] || {};
            const b = result.dynastyB || result[dynastyB] || {};
            div.innerHTML = `
                <h3 style="font-size:0.9rem;color:#e94560;margin-bottom:10px;padding-bottom:6px;border-bottom:1px solid #2a2a4a;">朝代对比</h3>
                <div style="overflow-x:auto;">
                    <table style="width:100%;font-size:0.8rem;border-collapse:collapse;">
                        <thead>
                            <tr style="background:#0f172a;">
                                <th style="padding:8px;text-align:left;color:#aaa;border-bottom:1px solid #2a2a4a;">指标</th>
                                <th style="padding:8px;text-align:center;color:${this.dynastyColors[dynastyA] || '#e94560'};border-bottom:1px solid #2a2a4a;">${aName}</th>
                                <th style="padding:8px;text-align:center;color:${this.dynastyColors[dynastyB] || '#e94560'};border-bottom:1px solid #2a2a4a;">${bName}</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td style="padding:6px;color:#aaa;border-bottom:1px solid #1e1e3a;">起止年份</td>
                                <td style="padding:6px;text-align:center;border-bottom:1px solid #1e1e3a;">${a.startYear || a.years || '-'}</td>
                                <td style="padding:6px;text-align:center;border-bottom:1px solid #1e1e3a;">${b.startYear || b.years || '-'}</td>
                            </tr>
                            <tr>
                                <td style="padding:6px;color:#aaa;border-bottom:1px solid #1e1e3a;">路线数</td>
                                <td style="padding:6px;text-align:center;border-bottom:1px solid #1e1e3a;">${a.routeCount || a.routes || 0}</td>
                                <td style="padding:6px;text-align:center;border-bottom:1px solid #1e1e3a;">${b.routeCount || b.routes || 0}</td>
                            </tr>
                            <tr>
                                <td style="padding:6px;color:#aaa;border-bottom:1px solid #1e1e3a;">总距离 (km)</td>
                                <td style="padding:6px;text-align:center;border-bottom:1px solid #1e1e3a;">${(a.totalDistanceKm || a.totalDistance || 0).toFixed(0)}</td>
                                <td style="padding:6px;text-align:center;border-bottom:1px solid #1e1e3a;">${(b.totalDistanceKm || b.totalDistance || 0).toFixed(0)}</td>
                            </tr>
                            <tr>
                                <td style="padding:6px;color:#aaa;border-bottom:1px solid #1e1e3a;">平均贸易得分</td>
                                <td style="padding:6px;text-align:center;border-bottom:1px solid #1e1e3a;">${((a.avgTradeScore || a.tradeScore || a.trade || 0) * 100).toFixed(0)}%</td>
                                <td style="padding:6px;text-align:center;border-bottom:1px solid #1e1e3a;">${((b.avgTradeScore || b.tradeScore || b.trade || 0) * 100).toFixed(0)}%</td>
                            </tr>
                            <tr>
                                <td style="padding:6px;color:#aaa;">平均文化得分</td>
                                <td style="padding:6px;text-align:center;">${((a.avgCultureScore || a.cultureScore || a.culture || 0) * 100).toFixed(0)}%</td>
                                <td style="padding:6px;text-align:center;">${((b.avgCultureScore || b.cultureScore || b.culture || 0) * 100).toFixed(0)}%</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            `;
        } catch (error) {
            console.error('朝代对比失败:', error);
            div.innerHTML = '<div class="empty-state">对比失败</div>';
        }
    }
};

window.DynastyPanel = DynastyPanel;
