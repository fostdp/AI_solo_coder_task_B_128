const VirtualCaravanPanel = {
    container: null,
    currentCaravan: null,
    wsStatusSubscription: null,
    wsEventsSubscription: null,
    pollTimer: null,
    eventCache: [],
    simulationSpeed: 1,
    speedLevels: [
        { value: 1, label: '慢速 1x', color: '#60a5fa' },
        { value: 2, label: '正常 2x', color: '#4ade80' },
        { value: 5, label: '快速 5x', color: '#fbbf24' },
        { value: 10, label: '极速 10x', color: '#ef4444' }
    ],
    waypoints: [
        { id: 1, name: '长安', lng: 108.94, lat: 34.26 },
        { id: 2, name: '兰州', lng: 103.83, lat: 36.06 },
        { id: 3, name: '武威', lng: 102.64, lat: 37.93 },
        { id: 4, name: '张掖', lng: 100.45, lat: 38.93 },
        { id: 5, name: '酒泉', lng: 98.50, lat: 39.73 },
        { id: 6, name: '敦煌', lng: 94.66, lat: 40.14 },
        { id: 7, name: '楼兰', lng: 89.95, lat: 40.56 }
    ],
    camelTypes: {
        'BACTRIAN': { name: '双峰驼', speedKmh: 5.0, endurance: 85 },
        'DROMEDARY': { name: '单峰驼', speedKmh: 6.5, endurance: 70 },
        'HYBRID': { name: '杂交驼', speedKmh: 4.5, endurance: 90 },
        'WILD_BACTRIAN': { name: '野双峰驼', speedKmh: 7.0, endurance: 75 },
        'PACK_SMALL': { name: '小型驮队驼', speedKmh: 5.5, endurance: 80 }
    },
    cargoNames: {
        'SILK': '丝绸', 'SPICE': '香料', 'JADE': '玉石', 'TEA': '茶叶',
        'PORCELAIN': '瓷器', 'HORSE': '马匹', 'GOLD_SILVER': '金银', 'GENERAL': '普通'
    },
    seasonNames: { 'SPRING': '春', 'SUMMER': '夏', 'AUTUMN': '秋', 'WINTER': '冬' },
    statusLabels: {
        'PREPARING': '准备中', 'TRAVELING': '旅行中',
        'RESTING': '休整中', 'COMPLETED': '已完成', 'STRANDED': '受困'
    },
    statusColors: {
        'PREPARING': '#fbbf24', 'TRAVELING': '#4ade80',
        'RESTING': '#60a5fa', 'COMPLETED': '#a855f7', 'STRANDED': '#ef4444'
    },
    severityColors: {
        'POSITIVE': '#4ade80', 'INFO': '#60a5fa',
        'WARNING': '#fbbf24', 'DANGER': '#ef4444'
    },
    severityIcons: {
        'POSITIVE': '✅', 'INFO': 'ℹ️', 'WARNING': '⚠️', 'DANGER': '🚨'
    },

    init(containerId) {
        this.container = document.getElementById(containerId);
        if (!this.container) return;
        this.render();
    },

    render() {
        this.container.innerHTML = `
            <div class="panel">
                <h2>🐫 虚拟驼队旅行</h2>
                <div style="display:flex;margin-bottom:12px;background:#0f172a;border-radius:6px;padding:3px;">
                    <button class="tab-btn active" data-tab="create" id="tabCreate"
                        style="flex:1;padding:8px;border:none;background:transparent;color:#e0e0e0;cursor:pointer;border-radius:4px;font-size:0.85rem;transition:all 0.2s;">
                        🏕️ 创建驼队
                    </button>
                    <button class="tab-btn" data-tab="journey" id="tabJourney"
                        style="flex:1;padding:8px;border:none;background:transparent;color:#888;cursor:pointer;border-radius:4px;font-size:0.85rem;transition:all 0.2s;">
                        🚶 我的旅程
                    </button>
                </div>
                <div id="tabCreateContent">
                    ${this.renderCreateForm()}
                </div>
                <div id="tabJourneyContent" style="display:none;">
                    <div id="caravanStatusSection">
                        <div class="empty-state">暂无活跃驼队，请先创建</div>
                    </div>
                </div>
            </div>
        `;
        this.bindTabEvents();
        this.bindCreateFormEvents();
        this.updateTabStyles('create');
    },

    renderCreateForm() {
        return `
            <div class="form-group">
                <label>驼队名称</label>
                <input type="text" class="form-control" id="caravanName" placeholder="例如：西行商队" maxlength="30" value="丝路商队">
            </div>
            <div class="form-group">
                <label>队长姓名</label>
                <input type="text" class="form-control" id="captainName" placeholder="例如：张三" maxlength="20" value="李队长">
            </div>
            <div class="form-group">
                <label>选择路线</label>
                <select class="form-control" id="vcRouteSelect">
                    <option value="1">长安-敦煌主线 (1800km)</option>
                    <option value="2">敦煌-楼兰道 (500km)</option>
                    <option value="3">楼兰-龟兹道 (600km)</option>
                    <option value="4">龟兹-疏勒道 (700km)</option>
                    <option value="5">疏勒-大宛道 (650km)</option>
                    <option value="6">大宛-波斯道 (800km)</option>
                </select>
            </div>
            <div class="form-group">
                <label>货物类型</label>
                <select class="form-control" id="vcCargoType">
                    <option value="SILK">丝绸</option>
                    <option value="SPICE">香料</option>
                    <option value="JADE">玉石</option>
                    <option value="TEA">茶叶</option>
                    <option value="PORCELAIN">瓷器</option>
                    <option value="HORSE">马匹</option>
                    <option value="GOLD_SILVER">金银</option>
                    <option value="GENERAL">普通货物</option>
                </select>
            </div>
            <div class="form-group">
                <label>货物重量 (kg)</label>
                <input type="number" class="form-control" id="vcCargoWeight" value="1500" min="100" max="50000">
            </div>
            <div class="form-group">
                <label>骆驼种类</label>
                <select class="form-control" id="vcCamelType">
                    ${Object.entries(this.camelTypes).map(([code, info]) => 
                        `<option value="${code}">${info.name} (${info.speedKmh}km/h)</option>`
                    ).join('')}
                </select>
            </div>
            <div class="form-group">
                <label>骆驼数量</label>
                <input type="number" class="form-control" id="vcCamelCount" value="12" min="1" max="200">
            </div>
            <div class="form-group">
                <label>人员数量</label>
                <input type="number" class="form-control" id="vcCrewCount" value="6" min="1" max="100">
            </div>
            <div class="form-group">
                <label>出发季节</label>
                <select class="form-control" id="vcSeason">
                    <option value="SPRING">🌸 春季</option>
                    <option value="SUMMER">☀️ 夏季</option>
                    <option value="AUTUMN">🍂 秋季</option>
                    <option value="WINTER">❄️ 冬季</option>
                </select>
            </div>
            <div class="form-group checkbox-group">
                <input type="checkbox" id="vcIsPublic" checked>
                <label for="vcIsPublic">公开旅程（其他玩家可见）</label>
            </div>
            <button class="btn btn-primary" id="createCaravanBtn">🚀 创建驼队并出发</button>
        `;
    },

    bindTabEvents() {
        document.getElementById('tabCreate').addEventListener('click', () => {
            this.switchTab('create');
        });
        document.getElementById('tabJourney').addEventListener('click', () => {
            this.switchTab('journey');
        });
    },

    bindCreateFormEvents() {
        const btn = document.getElementById('createCaravanBtn');
        if (btn) btn.addEventListener('click', () => this.createCaravan());
    },

    switchTab(tab) {
        const createEl = document.getElementById('tabCreateContent');
        const journeyEl = document.getElementById('tabJourneyContent');
        if (tab === 'create') {
            createEl.style.display = '';
            journeyEl.style.display = 'none';
        } else {
            createEl.style.display = 'none';
            journeyEl.style.display = '';
            if (this.currentCaravan) {
                this.loadJourneyData();
            }
        }
        this.updateTabStyles(tab);
    },

    updateTabStyles(tab) {
        const createBtn = document.getElementById('tabCreate');
        const journeyBtn = document.getElementById('tabJourney');
        if (!createBtn || !journeyBtn) return;
        if (tab === 'create') {
            createBtn.style.background = 'linear-gradient(135deg, #e94560 0%, #c73659 100%)';
            createBtn.style.color = '#fff';
            createBtn.style.fontWeight = '600';
            journeyBtn.style.background = 'transparent';
            journeyBtn.style.color = '#888';
            journeyBtn.style.fontWeight = '400';
        } else {
            journeyBtn.style.background = 'linear-gradient(135deg, #e94560 0%, #c73659 100%)';
            journeyBtn.style.color = '#fff';
            journeyBtn.style.fontWeight = '600';
            createBtn.style.background = 'transparent';
            createBtn.style.color = '#888';
            createBtn.style.fontWeight = '400';
        }
    },

    async createCaravan() {
        const btn = document.getElementById('createCaravanBtn');
        const name = document.getElementById('caravanName').value.trim();
        const captain = document.getElementById('captainName').value.trim();
        if (!name || !captain) { alert('请填写驼队名称和队长姓名'); return; }
        btn.textContent = '创建中...';
        btn.disabled = true;
        try {
            const req = {
                name: name,
                captainName: captain,
                routeId: parseInt(document.getElementById('vcRouteSelect').value) || 1,
                cargoType: document.getElementById('vcCargoType').value,
                cargoWeightKg: parseFloat(document.getElementById('vcCargoWeight').value) || 1500,
                camelType: document.getElementById('vcCamelType').value || 'BACTRIAN',
                camelCount: parseInt(document.getElementById('vcCamelCount').value) || 10,
                crewCount: parseInt(document.getElementById('vcCrewCount').value) || 5,
                season: document.getElementById('vcSeason').value,
                isPublic: document.getElementById('vcIsPublic').checked
            };
            const caravan = await API.createVirtualCaravan(req);
            this.currentCaravan = caravan;
            this.connectVirtualCaravanWS(caravan.id || caravan.caravanId);
            this.startPolling(caravan.id || caravan.caravanId);
            this.switchTab('journey');
            this.renderCaravanStatus(caravan);
            this.updateMapMarker(caravan);
        } catch (error) {
            console.error('创建驼队失败:', error);
            this.createMockCaravan(name, captain);
        } finally {
            btn.textContent = '🚀 创建驼队并出发';
            btn.disabled = false;
        }
    },

    createMockCaravan(name, captain) {
        const camelType = document.getElementById('vcCamelType')?.value || 'BACTRIAN';
        const camelInfo = this.camelTypes[camelType] || this.camelTypes.BACTRIAN;
        const caravan = {
            id: Date.now(),
            name: name,
            captainName: captain,
            routeId: parseInt(document.getElementById('vcRouteSelect').value) || 1,
            cargoType: document.getElementById('vcCargoType').value,
            camelType: camelType,
            camelSpeedKmh: camelInfo.speedKmh,
            status: 'PREPARING',
            progressPct: 0,
            distanceTraveledKm: 0,
            totalDistanceKm: 1800,
            daysElapsed: 0,
            totalDays: 60,
            waterRemainingLiters: 5000,
            waterCapacityLiters: 5000,
            foodRemainingDays: 45,
            foodTotalDays: 45,
            moralePct: 85,
            goldCoins: 1000,
            currentWaypointIndex: 0,
            lng: 108.94,
            lat: 34.26
        };
        this.currentCaravan = caravan;
        this.startPolling(caravan.id);
        this.switchTab('journey');
        this.renderCaravanStatus(caravan);
        this.updateMapMarker(caravan);
        setTimeout(() => this.simulateMockProgress(), 2000);
    },

    simulateMockProgress() {
        if (!this.currentCaravan || this.currentCaravan.status === 'COMPLETED') return;
        if (this.currentCaravan.status === 'TRAVELING') {
            const progress = Math.min(100, this.currentCaravan.progressPct + Math.random() * 1.5);
            this.currentCaravan.progressPct = progress;
            this.currentCaravan.distanceTraveledKm = (this.currentCaravan.totalDistanceKm || 1800) * progress / 100;
            this.currentCaravan.daysElapsed = Math.floor(progress * 0.6);
            this.currentCaravan.waterRemainingLiters = Math.max(0, this.currentCaravan.waterCapacityLiters - (progress * 45));
            this.currentCaravan.foodRemainingDays = Math.max(0, 45 - progress * 0.5);
            this.currentCaravan.moralePct = Math.max(10, 85 - Math.random() * progress * 0.5);
            const routeCoords = [
                [108.94, 34.26], [106.16, 34.73], [103.83, 36.06],
                [102.64, 37.43], [100.45, 38.93], [97.14, 39.73], [94.66, 40.14]
            ];
            const idx = Math.min(routeCoords.length - 1, Math.floor(progress / 100 * (routeCoords.length - 1)));
            const frac = (progress / 100 * (routeCoords.length - 1)) - idx;
            const next = Math.min(routeCoords.length - 1, idx + 1);
            this.currentCaravan.lng = routeCoords[idx][0] + (routeCoords[next][0] - routeCoords[idx][0]) * frac;
            this.currentCaravan.lat = routeCoords[idx][1] + (routeCoords[next][1] - routeCoords[idx][1]) * frac;
            if (progress >= 100) this.currentCaravan.status = 'COMPLETED';
            if (Math.random() < 0.2) {
                const severities = ['POSITIVE', 'INFO', 'WARNING'];
                const sev = severities[Math.floor(Math.random() * severities.length)];
                const msgs = {
                    'POSITIVE': ['在绿洲补充了水源！士气提升', '商队交易成功，获得金币', '发现捷径，节省2天时间'],
                    'INFO': ['经过重要补给点', '与另一支驼队相遇', '当地官员接见了商队'],
                    'WARNING': ['遭遇沙尘暴，行进缓慢', '骆驼劳累，需要休整', '水源检测发现杂质']
                };
                this.addEvent({
                    id: Date.now(),
                    severity: sev,
                    message: msgs[sev][Math.floor(Math.random() * 3)],
                    timestamp: new Date().toISOString()
                });
            }
            this.renderCaravanStatus(this.currentCaravan);
            this.updateMapMarker(this.currentCaravan);
        }
        setTimeout(() => this.simulateMockProgress(), 3000);
    },

    async loadJourneyData() {
        if (!this.currentCaravan) return;
        const id = this.currentCaravan.id || this.currentCaravan.caravanId;
        try {
            const status = await API.getVirtualCaravan(id);
            if (status) {
                this.currentCaravan = { ...this.currentCaravan, ...status };
                this.renderCaravanStatus(this.currentCaravan);
                this.updateMapMarker(this.currentCaravan);
            }
            const events = await API.getVirtualCaravanEvents(id, 30);
            this.eventCache = Array.isArray(events) ? events : this.eventCache;
            this.renderEvents();
        } catch (error) {
            console.error('加载旅程数据失败:', error);
        }
    },

    connectVirtualCaravanWS(caravanId) {
        try {
            if (!window.App || !window.App.stompClient || !window.App.stompClient.connected) {
                console.warn('STOMP未连接，将依赖轮询');
                return;
            }
            this.disconnectWS();
            this.wsStatusSubscription = window.App.stompClient.subscribe(
                `/user/topic/virtual-caravans/${caravanId}/status`,
                (message) => {
                    try {
                        const status = JSON.parse(message.body);
                        this.currentCaravan = { ...this.currentCaravan, ...status };
                        this.renderCaravanStatus(this.currentCaravan);
                        this.updateMapMarker(this.currentCaravan);
                    } catch (e) { console.error('WS状态解析失败:', e); }
                }
            );
            this.wsEventsSubscription = window.App.stompClient.subscribe(
                `/user/topic/virtual-caravans/${caravanId}/events`,
                (message) => {
                    try {
                        const event = JSON.parse(message.body);
                        this.addEvent(event);
                    } catch (e) { console.error('WS事件解析失败:', e); }
                }
            );
        } catch (error) {
            console.error('WS连接失败:', error);
        }
    },

    disconnectWS() {
        try {
            if (this.wsStatusSubscription) { this.wsStatusSubscription.unsubscribe(); this.wsStatusSubscription = null; }
            if (this.wsEventsSubscription) { this.wsEventsSubscription.unsubscribe(); this.wsEventsSubscription = null; }
        } catch (e) { /* ignore */ }
    },

    startPolling(caravanId) {
        this.stopPolling();
        this.pollTimer = setInterval(async () => {
            try {
                const status = await API.getVirtualCaravan(caravanId);
                if (status) {
                    this.currentCaravan = { ...this.currentCaravan, ...status };
                    this.renderCaravanStatus(this.currentCaravan);
                    this.updateMapMarker(this.currentCaravan);
                }
            } catch (e) { /* ignore */ }
            try {
                const events = await API.getVirtualCaravanEvents(caravanId, 10);
                if (Array.isArray(events) && events.length) {
                    const existingIds = new Set(this.eventCache.map(e => e.id));
                    events.forEach(e => { if (!existingIds.has(e.id)) this.eventCache.unshift(e); });
                    this.eventCache = this.eventCache.slice(0, 50);
                    this.renderEvents();
                }
            } catch (e) { /* ignore */ }
        }, 30000);
    },

    stopPolling() {
        if (this.pollTimer) { clearInterval(this.pollTimer); this.pollTimer = null; }
    },

    updateMapMarker(caravan) {
        if (!caravan || !caravan.lng || !caravan.lat) return;
        try {
            if (window.SilkRoadMap && typeof window.SilkRoadMap.showVirtualCaravan === 'function') {
                window.SilkRoadMap.showVirtualCaravan(caravan.lng, caravan.lat, caravan.name || '驼队');
            }
        } catch (e) { console.error('更新地图标记失败:', e); }
    },

    renderCaravanStatus(caravan) {
        const div = document.getElementById('caravanStatusSection');
        if (!div || !caravan) return;
        const status = caravan.status || 'PREPARING';
        const statusColor = this.statusColors[status] || '#888';
        const progress = Math.min(100, caravan.progressPct || 0);
        const water = caravan.waterRemainingLiters || 0;
        const waterCap = caravan.waterCapacityLiters || water || 1;
        const waterPct = Math.min(100, (water / waterCap) * 100);
        const food = caravan.foodRemainingDays || 0;
        const foodTotal = caravan.foodTotalDays || food || 1;
        const foodPct = Math.min(100, (food / foodTotal) * 100);
        const morale = Math.min(100, caravan.moralePct || 0);
        const camelInfo = this.camelTypes[caravan.camelType] || this.camelTypes.BACTRIAN;
        const baseSpeed = caravan.camelSpeedKmh || camelInfo.speedKmh || 5;
        const currentSpeed = baseSpeed * this.simulationSpeed;
        const remainingDist = Math.max(0, (caravan.totalDistanceKm || 1800) - (caravan.distanceTraveledKm || 0));
        const remainingHours = currentSpeed > 0 ? remainingDist / currentSpeed : 0;
        const remainingDays = Math.floor(remainingHours / 24);
        const remainingHoursMod = Math.floor(remainingHours % 24);
        const remainingMinutes = Math.floor((remainingHours % 1) * 60);
        const nextWaypoint = this.waypoints[Math.min((caravan.currentWaypointIndex || 0) + 1, this.waypoints.length - 1)];

        div.innerHTML = `
            <div style="background:#0f172a;padding:12px;border-radius:6px;margin-bottom:12px;border-top:3px solid ${statusColor};">
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px;">
                    <div>
                        <div style="font-size:1rem;font-weight:600;color:#e94560;">🐫 ${caravan.name || '驼队'}</div>
                        <div style="font-size:0.75rem;color:#888;">队长: ${caravan.captainName || '-'} | 🐪 ${camelInfo.name}</div>
                    </div>
                    <span style="padding:4px 10px;border-radius:12px;font-size:0.75rem;font-weight:600;background:${statusColor}20;color:${statusColor};border:1px solid ${statusColor};">
                        ${this.statusLabels[status] || status}
                    </span>
                </div>
                <div style="margin-bottom:10px;">
                    <div style="display:flex;justify-content:space-between;font-size:0.75rem;margin-bottom:3px;">
                        <span style="color:#aaa;">🗺️ 旅程进度</span>
                        <span style="color:#e0e0e0;font-weight:500;">${progress.toFixed(1)}%</span>
                    </div>
                    <div style="height:18px;background:#2a2a4a;border-radius:9px;overflow:hidden;position:relative;">
                        <div style="height:100%;width:${progress}%;background:linear-gradient(90deg,#e94560,#c73659);transition:width 0.5s;"></div>
                        <span style="position:absolute;left:50%;top:50%;transform:translate(-50%,-50%);font-size:0.7rem;color:#fff;font-weight:600;text-shadow:0 0 4px #000;">
                            ${(caravan.distanceTraveledKm || 0).toFixed(0)} / ${(caravan.totalDistanceKm || 0).toFixed(0)} km
                        </span>
                    </div>
                    <div style="display:flex;justify-content:space-between;font-size:0.7rem;margin-top:3px;color:#888;">
                        <span>第 ${caravan.daysElapsed || 0} / ${caravan.totalDays || '?'} 天</span>
                        <span>🚩 ${caravan.cargoType ? (this.cargoNames[caravan.cargoType] || caravan.cargoType) : ''}</span>
                    </div>
                </div>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-bottom:10px;">
                    <div style="background:#1a1a2e;padding:8px;border-radius:5px;">
                        <div style="display:flex;justify-content:space-between;font-size:0.7rem;margin-bottom:3px;">
                            <span style="color:#aaa;">💧 水量</span>
                            <span style="color:${waterPct>50?'#60a5fa':waterPct>20?'#fbbf24':'#ef4444'};font-weight:600;">${water.toFixed(0)}L</span>
                        </div>
                        <div style="height:8px;background:#2a2a4a;border-radius:4px;overflow:hidden;">
                            <div style="height:100%;width:${waterPct}%;background:${waterPct>50?'#60a5fa':waterPct>20?'#fbbf24':'#ef4444'};"></div>
                        </div>
                    </div>
                    <div style="background:#1a1a2e;padding:8px;border-radius:5px;">
                        <div style="display:flex;justify-content:space-between;font-size:0.7rem;margin-bottom:3px;">
                            <span style="color:#aaa;">🍞 食物</span>
                            <span style="color:${foodPct>50?'#4ade80':foodPct>20?'#fbbf24':'#ef4444'};font-weight:600;">${food.toFixed(0)}天</span>
                        </div>
                        <div style="height:8px;background:#2a2a4a;border-radius:4px;overflow:hidden;">
                            <div style="height:100%;width:${foodPct}%;background:${foodPct>50?'#4ade80':foodPct>20?'#fbbf24':'#ef4444'};"></div>
                        </div>
                    </div>
                    <div style="background:#1a1a2e;padding:8px;border-radius:5px;">
                        <div style="display:flex;justify-content:space-between;font-size:0.7rem;margin-bottom:3px;">
                            <span style="color:#aaa;">😊 士气</span>
                            <span style="color:${morale>60?'#4ade80':morale>30?'#fbbf24':'#ef4444'};font-weight:600;">${morale.toFixed(0)}%</span>
                        </div>
                        <div style="height:8px;background:#2a2a4a;border-radius:4px;overflow:hidden;">
                            <div style="height:100%;width:${morale}%;background:${morale>60?'#4ade80':morale>30?'#fbbf24':'#ef4444'};"></div>
                        </div>
                    </div>
                    <div style="background:#1a1a2e;padding:8px;border-radius:5px;text-align:center;">
                        <div style="font-size:0.7rem;color:#aaa;margin-bottom:3px;">💰 金币</div>
                        <div style="font-size:1.1rem;color:#fbbf24;font-weight:700;">${(caravan.goldCoins || 0).toLocaleString()}</div>
                    </div>
                </div>
                <div style="background:#1a1a2e;padding:8px;border-radius:5px;margin-bottom:10px;">
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
                        <div style="font-size:0.75rem;color:#e94560;font-weight:600;">⏱️ 预计剩余时间</div>
                        <div style="font-size:0.8rem;color:#4ade80;font-weight:600;">
                            ${remainingDays > 0 ? remainingDays + '天 ' : ''}${remainingHoursMod}小时${remainingMinutes}分
                        </div>
                    </div>
                    <div style="font-size:0.7rem;color:#888;text-align:center;">
                        当前速度: ${currentSpeed.toFixed(1)} km/h (${this.simulationSpeed}x)
                    </div>
                </div>
                <div style="margin-bottom:10px;">
                    <div style="font-size:0.75rem;color:#aaa;margin-bottom:6px;">⚡ 模拟速度</div>
                    <div style="display:flex;gap:4px;">
                        ${this.speedLevels.map(s => `
                            <button class="speed-btn" data-speed="${s.value}"
                                style="flex:1;padding:6px 4px;border:1px solid ${this.simulationSpeed === s.value ? s.color : '#3a3a5a'};
                                    background:${this.simulationSpeed === s.value ? s.color + '20' : 'transparent'};
                                    color:${this.simulationSpeed === s.value ? s.color : '#888'};
                                    border-radius:4px;font-size:0.7rem;font-weight:600;cursor:pointer;transition:all 0.2s;">
                                ${s.label}
                            </button>
                        `).join('')}
                    </div>
                </div>
                <div style="display:flex;gap:6px;margin-bottom:10px;">
                    ${status === 'TRAVELING' ? `
                        <button class="btn btn-secondary" id="vcNextWaypointBtn" style="flex:1;font-size:0.75rem;">📍 下一驿站</button>
                        <button class="btn btn-secondary" id="vcNextEventBtn" style="flex:1;font-size:0.75rem;">🎲 下一事件</button>
                    ` : ''}
                </div>
                ${nextWaypoint ? `
                    <div style="font-size:0.7rem;color:#888;text-align:center;padding:4px;background:#1a1a2e;border-radius:4px;">
                        📍 下一站: ${nextWaypoint.name}
                    </div>
                ` : ''}
                <div style="display:flex;gap:6px;">
                    ${status === 'PREPARING' ? `
                        <button class="btn btn-primary" id="vcStartBtn" style="flex:1;">▶️ 出发！</button>
                    ` : ''}
                    ${status === 'TRAVELING' ? `
                        <button class="btn btn-secondary" id="vcPauseBtn" style="flex:1;">⏸️ 暂停</button>
                    ` : ''}
                    ${status === 'RESTING' ? `
                        <button class="btn btn-primary" id="vcResumeBtn" style="flex:1;">▶️ 继续</button>
                    ` : ''}
                    ${status === 'COMPLETED' ? `
                        <button class="btn btn-secondary" id="vcNewBtn" style="flex:1;">🏕️ 新建驼队</button>
                    ` : ''}
                </div>
            </div>
            <div style="background:#0f172a;padding:12px;border-radius:6px;">
                <h4 style="font-size:0.85rem;color:#e94560;margin-bottom:8px;padding-bottom:6px;border-bottom:1px solid #2a2a4a;">
                    📋 事件日志
                </h4>
                <div id="vcEventLog" style="max-height:250px;overflow-y:auto;"></div>
            </div>
        `;
        this.bindControlButtons(status);
        this.bindSpeedButtons();
        this.bindFastForwardButtons(status);
        this.renderEvents();
    },

    bindControlButtons(status) {
        const id = this.currentCaravan?.id || this.currentCaravan?.caravanId;
        if (status === 'PREPARING') {
            const btn = document.getElementById('vcStartBtn');
            if (btn) btn.addEventListener('click', async () => {
                btn.textContent = '启动中...';
                btn.disabled = true;
                try {
                    await API.startVirtualCaravan(id);
                    if (this.currentCaravan) this.currentCaravan.status = 'TRAVELING';
                    this.renderCaravanStatus(this.currentCaravan);
                } catch (error) {
                    console.error('启动失败:', error);
                    if (this.currentCaravan) {
                        this.currentCaravan.status = 'TRAVELING';
                        this.renderCaravanStatus(this.currentCaravan);
                    }
                }
            });
        } else if (status === 'TRAVELING') {
            const btn = document.getElementById('vcPauseBtn');
            if (btn) btn.addEventListener('click', async () => {
                btn.textContent = '暂停中...';
                btn.disabled = true;
                try {
                    await API.pauseVirtualCaravan(id);
                    if (this.currentCaravan) this.currentCaravan.status = 'RESTING';
                    this.renderCaravanStatus(this.currentCaravan);
                    this.addEvent({ id: Date.now(), severity: 'INFO', message: '驼队已暂停行进，开始休整', timestamp: new Date().toISOString() });
                } catch (error) {
                    console.error('暂停失败:', error);
                    if (this.currentCaravan) {
                        this.currentCaravan.status = 'RESTING';
                        this.renderCaravanStatus(this.currentCaravan);
                    }
                }
            });
        } else if (status === 'RESTING') {
            const btn = document.getElementById('vcResumeBtn');
            if (btn) btn.addEventListener('click', async () => {
                btn.textContent = '启动中...';
                btn.disabled = true;
                try {
                    await API.resumeVirtualCaravan(id);
                    if (this.currentCaravan) this.currentCaravan.status = 'TRAVELING';
                    this.renderCaravanStatus(this.currentCaravan);
                    this.addEvent({ id: Date.now(), severity: 'POSITIVE', message: '驼队休整完毕，继续前进！', timestamp: new Date().toISOString() });
                } catch (error) {
                    console.error('继续失败:', error);
                    if (this.currentCaravan) {
                        this.currentCaravan.status = 'TRAVELING';
                        this.renderCaravanStatus(this.currentCaravan);
                    }
                }
            });
        } else if (status === 'COMPLETED') {
            const btn = document.getElementById('vcNewBtn');
            if (btn) btn.addEventListener('click', () => {
                this.stopPolling();
                this.disconnectWS();
                this.currentCaravan = null;
                this.eventCache = [];
                this.switchTab('create');
            });
        }
    },

    addEvent(event) {
        if (!event) return;
        this.eventCache.unshift(event);
        this.eventCache = this.eventCache.slice(0, 50);
        this.renderEvents();
    },

    renderEvents() {
        const div = document.getElementById('vcEventLog');
        if (!div) return;
        const events = this.eventCache || [];
        if (events.length === 0) {
            div.innerHTML = '<div class="empty-state">暂无事件记录</div>';
            return;
        }
        const sorted = [...events].sort((a, b) =>
            new Date(b.timestamp || b.time || 0) - new Date(a.timestamp || a.time || 0)
        );
        div.innerHTML = sorted.map(e => {
            const sev = e.severity || 'INFO';
            const color = this.severityColors[sev] || '#60a5fa';
            const icon = this.severityIcons[sev] || 'ℹ️';
            const time = e.timestamp || e.time ? new Date(e.timestamp || e.time).toLocaleString('zh-CN') : '';
            return `
                <div style="padding:8px;margin-bottom:6px;background:#1a1a2e;border-radius:5px;border-left:3px solid ${color};">
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:3px;">
                        <span style="font-size:0.8rem;font-weight:500;color:${color};">${icon} ${sev}</span>
                        <span style="font-size:0.65rem;color:#666;">${time}</span>
                    </div>
                    <div style="font-size:0.8rem;color:#ccc;line-height:1.4;">${e.message || e.content || ''}</div>
                </div>
            `;
        }).join('');
    },

    bindSpeedButtons() {
        const buttons = document.querySelectorAll('.speed-btn');
        buttons.forEach(btn => {
            btn.addEventListener('click', () => {
                const speed = parseFloat(btn.dataset.speed);
                this.setSimulationSpeed(speed);
            });
        });
    },

    setSimulationSpeed(speed) {
        this.simulationSpeed = speed;
        try {
            if (API && typeof API.setCaravanSpeed === 'function' && this.currentCaravan) {
                API.setCaravanSpeed(this.currentCaravan.id, speed);
            }
        } catch (e) {
        }
        if (this.currentCaravan) {
            this.currentCaravan.simulationSpeed = speed;
            this.renderCaravanStatus(this.currentCaravan);
        }
    },

    bindFastForwardButtons(status) {
        if (status !== 'TRAVELING') return;
        const nextWpBtn = document.getElementById('vcNextWaypointBtn');
        if (nextWpBtn) {
            nextWpBtn.addEventListener('click', () => this.fastForwardToNextWaypoint());
        }
        const nextEventBtn = document.getElementById('vcNextEventBtn');
        if (nextEventBtn) {
            nextEventBtn.addEventListener('click', () => this.triggerRandomEvent());
        }
    },

    fastForwardToNextWaypoint() {
        if (!this.currentCaravan || this.currentCaravan.status !== 'TRAVELING') return;
        const currentIdx = this.currentCaravan.currentWaypointIndex || 0;
        const nextIdx = Math.min(currentIdx + 1, this.waypoints.length - 1);
        if (nextIdx === currentIdx) {
            this.addEvent({
                id: Date.now(),
                severity: 'INFO',
                message: '已到达终点，没有下一个驿站了',
                timestamp: new Date().toISOString()
            });
            return;
        }
        const nextWaypoint = this.waypoints[nextIdx];
        const totalDist = this.currentCaravan.totalDistanceKm || 1800;
        const progressPerWp = 100 / (this.waypoints.length - 1);
        const newProgress = Math.min(100, (nextIdx) * progressPerWp);
        this.currentCaravan.currentWaypointIndex = nextIdx;
        this.currentCaravan.progressPct = newProgress;
        this.currentCaravan.distanceTraveledKm = totalDist * newProgress / 100;
        this.currentCaravan.daysElapsed = Math.floor(newProgress * 0.6);
        this.currentCaravan.waterRemainingLiters = Math.max(0, this.currentCaravan.waterCapacityLiters - (newProgress * 45));
        this.currentCaravan.foodRemainingDays = Math.max(0, 45 - newProgress * 0.5);
        this.currentCaravan.lng = nextWaypoint.lng;
        this.currentCaravan.lat = nextWaypoint.lat;
        if (newProgress >= 100) this.currentCaravan.status = 'COMPLETED';
        this.addEvent({
            id: Date.now(),
            severity: 'POSITIVE',
            message: `快进抵达驿站: ${nextWaypoint.name}`,
            timestamp: new Date().toISOString()
        });
        this.renderCaravanStatus(this.currentCaravan);
        this.updateMapMarker(this.currentCaravan);
    },

    triggerRandomEvent() {
        if (!this.currentCaravan) return;
        const eventTypes = [
            { severity: 'POSITIVE', messages: ['在绿洲补充了水源！士气提升', '商队交易成功，获得金币', '发现捷径，节省时间', '遇到友好商队，交换情报'] },
            { severity: 'INFO', messages: ['经过重要补给点', '与另一支驼队相遇', '当地官员接见了商队', '发现有趣的风土人情'] },
            { severity: 'WARNING', messages: ['遭遇沙尘暴，行进缓慢', '骆驼劳累，需要休整', '水源检测发现杂质', '道路崎岖，小心前行'] },
            { severity: 'DANGER', messages: ['遭遇山贼袭击！损失部分货物', '骆驼生病，需要停留治疗', '暴雨冲毁路段，需绕道', '粮草受潮，部分损坏'] }
        ];
        const weights = [0.3, 0.4, 0.2, 0.1];
        let rand = Math.random();
        let typeIdx = 0;
        for (let i = 0; i < weights.length; i++) {
            if (rand < weights[i]) { typeIdx = i; break; }
            rand -= weights[i];
        }
        const eventType = eventTypes[typeIdx];
        const message = eventType.messages[Math.floor(Math.random() * eventType.messages.length)];
        const sev = ['POSITIVE', 'INFO', 'WARNING', 'DANGER'][typeIdx];
        if (sev === 'POSITIVE') {
            this.currentCaravan.goldCoins = (this.currentCaravan.goldCoins || 0) + Math.floor(Math.random() * 100);
            this.currentCaravan.moralePct = Math.min(100, (this.currentCaravan.moralePct || 80) + 5);
        } else if (sev === 'DANGER') {
            this.currentCaravan.moralePct = Math.max(10, (this.currentCaravan.moralePct || 80) - 10);
        }
        this.addEvent({
            id: Date.now(),
            severity: sev,
            message: message,
            timestamp: new Date().toISOString()
        });
        this.renderCaravanStatus(this.currentCaravan);
    }
};

window.VirtualCaravanPanel = VirtualCaravanPanel;
