const RouteComparatorPanel = {
    container: null,
    modernRoads: [],
    allComparisons: [],
    displayMode: 'BOTH',
    roadGradeFilter: 'ALL',
    modes: [
        { value: 'MODERN', label: '按现代公路对比' },
        { value: 'ANCIENT', label: '按古代路线对比' },
        { value: 'ALL', label: '全部对比' }
    ],
    roadGrades: [
        { value: 'ALL', label: '全部等级' },
        { value: 'EXPRESSWAY', label: '高速公路' },
        { value: 'NATIONAL', label: '国道' },
        { value: 'PROVINCIAL', label: '省道' },
        { value: 'INTERNATIONAL', label: '国际通道' }
    ],
    roadGradeNames: {
        'EXPRESSWAY': '高速',
        'NATIONAL': '国道',
        'PROVINCIAL': '省道',
        'INTERNATIONAL': '国际通道',
        'COUNTY': '县道',
        'OTHER': '其他'
    },
    roadGradeColors: {
        'EXPRESSWAY': '#ef4444',
        'NATIONAL': '#f97316',
        'PROVINCIAL': '#eab308',
        'INTERNATIONAL': '#a855f7',
        'COUNTY': '#3b82f6',
        'OTHER': '#6b7280'
    },
    pavementTypes: {
        'ASPHALT': '沥青路面',
        'CONCRETE': '水泥路面',
        'GRAVEL': '砂石路面',
        'DIRT': '土路'
    },
    adminLevels: {
        'NATIONAL': '国家级',
        'PROVINCIAL': '省级',
        'CITY': '市级',
        'COUNTY': '县级'
    },
    mockRoadDetails: {
        1: { standardCode: 'G30', roadGrade: 'EXPRESSWAY', pavementType: 'ASPHALT', designSpeedKmh: 120, laneWidthM: 3.75, adminLevel: 'NATIONAL', openYear: 2014, standardName: '国家高速公路网规划' },
        2: { standardCode: 'G7', roadGrade: 'EXPRESSWAY', pavementType: 'ASPHALT', designSpeedKmh: 100, laneWidthM: 3.75, adminLevel: 'NATIONAL', openYear: 2017, standardName: '国家高速公路网规划' },
        3: { standardCode: 'G314', roadGrade: 'NATIONAL', pavementType: 'ASPHALT', designSpeedKmh: 80, laneWidthM: 3.5, adminLevel: 'NATIONAL', openYear: 1990, standardName: '国道干线公路网规划' },
        4: { standardCode: 'G315', roadGrade: 'NATIONAL', pavementType: 'ASPHALT', designSpeedKmh: 60, laneWidthM: 3.5, adminLevel: 'NATIONAL', openYear: 1985, standardName: '国道干线公路网规划' }
    },

    init(containerId) {
        this.container = document.getElementById(containerId);
        if (!this.container) return;
        this.render();
        this.loadModernRoads();
    },

    render() {
        this.container.innerHTML = `
            <div class="panel">
                <h2>🛣️ 古今路线对比</h2>
                <div class="form-group">
                    <label>对比模式</label>
                    <select class="form-control" id="compareModeSelect">
                        ${this.modes.map(m => `<option value="${m.value}">${m.label}</option>`).join('')}
                    </select>
                </div>
                <div id="modernRoadSection" class="form-group">
                    <label>选择现代公路</label>
                    <select class="form-control" id="modernRoadSelect">
                        <option value="">加载中...</option>
                    </select>
                </div>
                <div id="roadGradeFilterSection" class="form-group">
                    <label>公路等级筛选</label>
                    <select class="form-control" id="roadGradeFilter">
                        ${this.roadGrades.map(g => `<option value="${g.value}">${g.label}</option>`).join('')}
                    </select>
                </div>
                <div id="ancientRouteSection" class="form-group hidden">
                    <label>选择古代路线</label>
                    <select class="form-control" id="ancientRouteSelect">
                        <option value="">加载中...</option>
                    </select>
                </div>
                <div style="display:flex;gap:8px;margin-bottom:12px;">
                    <button class="btn btn-primary" id="compareBtn">执行对比</button>
                    <button class="btn btn-secondary" id="toggleDisplayBtn">切换显示模式</button>
                </div>
                <button class="btn btn-secondary" id="showAllBtn" style="margin-bottom:12px;">显示所有对比</button>
                <div id="compareResult" style="margin-top:15px;"></div>
                <div id="allComparisons" style="margin-top:15px;"></div>
            </div>
        `;
        this.bindEvents();
    },

    bindEvents() {
        document.getElementById('compareModeSelect').addEventListener('change', (e) => {
            const mode = e.target.value;
            document.getElementById('modernRoadSection').classList.toggle('hidden', mode !== 'MODERN');
            document.getElementById('roadGradeFilterSection').classList.toggle('hidden', mode === 'ANCIENT');
            document.getElementById('ancientRouteSection').classList.toggle('hidden', mode !== 'ANCIENT');
        });
        document.getElementById('roadGradeFilter').addEventListener('change', (e) => {
            this.roadGradeFilter = e.target.value;
            this.filterRoadsByGrade();
            if (this.allComparisons.length > 0) {
                this.renderAllComparisonsFiltered();
            }
        });
        document.getElementById('compareBtn').addEventListener('click', () => {
            const mode = document.getElementById('compareModeSelect').value;
            if (mode === 'MODERN') {
                const roadId = document.getElementById('modernRoadSelect').value;
                if (roadId) this.compareByRoad(roadId);
            } else if (mode === 'ANCIENT') {
                const routeId = document.getElementById('ancientRouteSelect').value;
                if (routeId) this.compareByAncientRoute(routeId);
            } else {
                this.showAllComparisons();
            }
        });
        document.getElementById('toggleDisplayBtn').addEventListener('click', () => this.toggleDisplayMode());
        document.getElementById('showAllBtn').addEventListener('click', () => this.showAllComparisons());
    },

    async loadModernRoads() {
        const select = document.getElementById('modernRoadSelect');
        const ancientSelect = document.getElementById('ancientRouteSelect');
        try {
            const roads = await API.getModernRoads();
            this.modernRoads = Array.isArray(roads) ? roads : [];
            select.innerHTML = this.modernRoads.length
                ? '<option value="">选择公路</option>' + this.modernRoads.map(r =>
                    `<option value="${r.id || r.roadId}">${r.name || r.roadName || '公路' + r.id} (${(r.distanceKm || 0).toFixed(0)}km)</option>`
                  ).join('')
                : '<option value="">暂无公路数据</option>';
            if (window.RiskPanel && window.RiskPanel.routes && window.RiskPanel.routes.length) {
                ancientSelect.innerHTML = '<option value="">选择路线</option>' +
                    window.RiskPanel.routes.map(r => `<option value="${r.id}">${r.name}</option>`).join('');
            } else {
                ancientSelect.innerHTML = `
                    <option value="1">长安-敦煌主线</option>
                    <option value="2">敦煌-楼兰道</option>
                    <option value="3">楼兰-龟兹道</option>
                    <option value="4">龟兹-疏勒道</option>
                    <option value="5">疏勒-大宛道</option>
                    <option value="6">大宛-波斯道</option>
                    <option value="7">敦煌-车师道（北）</option>
                    <option value="8">车师-北庭道</option>
                    <option value="9">北庭-碎叶道</option>
                    <option value="10">波斯-地中海道</option>
                `;
            }
        } catch (error) {
            console.error('加载现代公路失败:', error);
            select.innerHTML = `
                <option value="1">G30 连霍高速</option>
                <option value="2">G7 京新高速</option>
                <option value="3">G314 乌红线</option>
                <option value="4">G315 西莎线</option>
            `;
        }
    },

    async compareByRoad(roadId) {
        const div = document.getElementById('compareResult');
        div.innerHTML = '<div class="loading">加载中...</div>';
        try {
            const result = await API.compareByModernRoad(roadId);
            this.renderComparisonCard(result);
            if (window.SilkRoadMap && typeof window.SilkRoadMap.showAncientVsModernComparison === 'function') {
                window.SilkRoadMap.showAncientVsModernComparison(
                    result.ancientCoords || result.ancientCoordinates,
                    result.modernCoords || result.modernCoordinates
                );
            }
        } catch (error) {
            console.error('路线对比失败:', error);
            this.renderFallbackComparison(roadId);
        }
    },

    async compareByAncientRoute(routeId) {
        const div = document.getElementById('compareResult');
        div.innerHTML = '<div class="loading">加载中...</div>';
        try {
            const result = await API.compareByAncientRoute(routeId);
            this.renderComparisonCard(result);
            if (window.SilkRoadMap && typeof window.SilkRoadMap.showAncientVsModernComparison === 'function') {
                window.SilkRoadMap.showAncientVsModernComparison(
                    result.ancientCoords || result.ancientCoordinates,
                    result.modernCoords || result.modernCoordinates
                );
            }
        } catch (error) {
            console.error('路线对比失败:', error);
            this.renderFallbackComparison(routeId, true);
        }
    },

    renderFallbackComparison(id, isAncient = false) {
        const ancientDist = isAncient ? (1200 + Math.random() * 800) : (1500 + Math.random() * 1000);
        const modernDist = ancientDist * (0.65 + Math.random() * 0.15);
        const ancientDays = Math.round(ancientDist / 30);
        const modernHours = Math.round(modernDist / 80);
        const water = Math.round(ancientDays * 350);
        const overlap = 30 + Math.random() * 50;
        this.renderComparisonCard({
            ancientRouteName: isAncient ? '古代丝路' : '古代商道',
            modernRoadName: !isAncient ? '现代公路' : '对应现代公路',
            ancientDistanceKm: ancientDist,
            modernDistanceKm: modernDist,
            ancientTravelDays: ancientDays,
            modernTravelHours: modernHours,
            ancientWaterRequiredLiters: water,
            routeOverlapPct: overlap,
            analysis: [
                `古代路线全长约 ${ancientDist.toFixed(0)} 公里，现代公路缩短至约 ${modernDist.toFixed(0)} 公里，节省约 ${((1 - modernDist/ancientDist)*100).toFixed(0)}% 距离。`,
                `古代驼队需行走约 ${ancientDays} 天，现代驾车仅需约 ${modernHours} 小时，效率提升约 ${((ancientDays*24/modernHours)).toFixed(0)} 倍。`,
                `古今路线重合度约 ${overlap.toFixed(0)}%，现代公路多取直穿山。`
            ],
            ancientCoords: [
                [108.94, 34.26], [104.16, 36.06], [102.64, 37.43],
                [97.14, 39.73], [94.66, 40.14], [89.55, 40.51]
            ],
            modernCoords: [
                [108.94, 34.26], [106.23, 35.26], [103.83, 36.06],
                [98.50, 39.73], [94.66, 40.14], [89.55, 40.51]
            ]
        });
    },

    renderComparisonCard(result) {
        const div = document.getElementById('compareResult');
        if (!result) {
            div.innerHTML = '<div class="empty-state">暂无对比数据</div>';
            return;
        }
        const ancientDist = result.ancientDistanceKm || result.ancientDistance || 0;
        const modernDist = result.modernDistanceKm || result.modernDistance || 0;
        const maxDist = Math.max(ancientDist, modernDist, 1);
        const ancientPct = (ancientDist / maxDist) * 100;
        const modernPct = (modernDist / maxDist) * 100;
        const ancientDays = result.ancientTravelDays || result.travelDays || 0;
        const modernHours = result.modernTravelHours || result.modernHours || 0;
        const water = result.ancientWaterRequiredLiters || result.waterRequired || 0;
        const overlap = (result.routeOverlapPct || result.overlap || 0) * (result.routeOverlapPct <= 1 ? 100 : 1);
        const analysis = result.analysis || result.analyses || [];
        const roadId = result.id || result.roadId || result.modernRoadId || 1;
        const roadDetails = result.roadDetails || this.mockRoadDetails[roadId] || this.mockRoadDetails[1];
        const gradeName = this.roadGradeNames[roadDetails.roadGrade] || roadDetails.roadGrade || '公路';
        const gradeColor = this.roadGradeColors[roadDetails.roadGrade] || '#6b7280';
        const pavementName = this.pavementTypes[roadDetails.pavementType] || roadDetails.pavementType || '沥青路面';
        const adminName = this.adminLevels[roadDetails.adminLevel] || roadDetails.adminLevel || '国家级';

        div.innerHTML = `
            <div style="background:#0f172a;padding:12px;border-radius:6px;border-left:4px solid #e94560;">
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px;">
                    <div style="display:flex;align-items:center;gap:8px;">
                        <h4 style="color:#e94560;font-size:0.9rem;margin:0;">
                            ${result.ancientRouteName || '古代路线'} ⚔️ ${result.modernRoadName || '现代公路'}
                        </h4>
                        <span style="padding:2px 8px;border-radius:4px;background:${gradeColor}20;color:${gradeColor};font-size:0.7rem;font-weight:600;border:1px solid ${gradeColor};">
                            ${roadDetails.standardCode || ''}
                        </span>
                    </div>
                    <span style="font-size:0.7rem;color:#888;">显示模式: ${this.getDisplayLabel()}</span>
                </div>
                <div style="margin-bottom:12px;padding:10px;background:#1a1a2e;border-radius:5px;">
                    <div style="font-size:0.8rem;color:#e94560;font-weight:600;margin-bottom:8px;">🛣️ 公路标准化信息</div>
                    <div style="display:grid;grid-template-columns:1fr 1fr;gap:6px;font-size:0.75rem;">
                        <div style="display:flex;justify-content:space-between;">
                            <span style="color:#888;">标准编号:</span>
                            <span style="color:#e0e0e0;font-weight:500;">${roadDetails.standardCode || '-'}</span>
                        </div>
                        <div style="display:flex;justify-content:space-between;">
                            <span style="color:#888;">公路等级:</span>
                            <span style="color:${gradeColor};font-weight:500;">${gradeName}</span>
                        </div>
                        <div style="display:flex;justify-content:space-between;">
                            <span style="color:#888;">路面类型:</span>
                            <span style="color:#e0e0e0;">${pavementName}</span>
                        </div>
                        <div style="display:flex;justify-content:space-between;">
                            <span style="color:#888;">设计时速:</span>
                            <span style="color:#4ade80;font-weight:500;">${roadDetails.designSpeedKmh || '-'} km/h</span>
                        </div>
                        <div style="display:flex;justify-content:space-between;">
                            <span style="color:#888;">车道宽度:</span>
                            <span style="color:#e0e0e0;">${roadDetails.laneWidthM || '-'} m</span>
                        </div>
                        <div style="display:flex;justify-content:space-between;">
                            <span style="color:#888;">行政等级:</span>
                            <span style="color:#e0e0e0;">${adminName}</span>
                        </div>
                        <div style="display:flex;justify-content:space-between;">
                            <span style="color:#888;">通车年份:</span>
                            <span style="color:#e0e0e0;">${roadDetails.openYear || '-'}</span>
                        </div>
                        <div style="display:flex;justify-content:space-between;">
                            <span style="color:#888;">标准名称:</span>
                            <span style="color:#e0e0e0;">${roadDetails.standardName || '-'}</span>
                        </div>
                    </div>
                </div>
                <div style="margin-bottom:12px;">
                    <div style="font-size:0.8rem;color:#aaa;margin-bottom:4px;">距离对比</div>
                    <div style="margin-bottom:6px;">
                        <div style="display:flex;justify-content:space-between;font-size:0.75rem;margin-bottom:2px;">
                            <span style="color:#fbbf24;">🏛️ 古代</span>
                            <span style="color:#e0e0e0;">${ancientDist.toFixed(0)} km</span>
                        </div>
                        <div style="height:14px;background:#2a2a4a;border-radius:7px;overflow:hidden;">
                            <div style="height:100%;width:${ancientPct}%;background:linear-gradient(90deg,#fbbf24,#f59e0b);transition:width 0.4s;"></div>
                        </div>
                    </div>
                    <div>
                        <div style="display:flex;justify-content:space-between;font-size:0.75rem;margin-bottom:2px;">
                            <span style="color:#60a5fa;">🛣️ 现代</span>
                            <span style="color:#e0e0e0;">${modernDist.toFixed(0)} km</span>
                        </div>
                        <div style="height:14px;background:#2a2a4a;border-radius:7px;overflow:hidden;">
                            <div style="height:100%;width:${modernPct}%;background:linear-gradient(90deg,#60a5fa,#3b82f6);transition:width 0.4s;"></div>
                        </div>
                    </div>
                    <div style="text-align:center;margin-top:6px;font-size:0.75rem;color:#4ade80;">
                        缩短 ${Math.max(0, ancientDist - modernDist).toFixed(0)} km (${ancientDist? ((1-modernDist/ancientDist)*100).toFixed(0):0}%)
                    </div>
                </div>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-bottom:12px;">
                    <div style="background:#1a1a2e;padding:8px;border-radius:4px;text-align:center;">
                        <div style="font-size:0.7rem;color:#aaa;">古代时间</div>
                        <div style="font-size:1.1rem;color:#fbbf24;font-weight:600;">${ancientDays} 天</div>
                    </div>
                    <div style="background:#1a1a2e;padding:8px;border-radius:4px;text-align:center;">
                        <div style="font-size:0.7rem;color:#aaa;">现代时间</div>
                        <div style="font-size:1.1rem;color:#60a5fa;font-weight:600;">${modernHours} 小时</div>
                    </div>
                </div>
                <div style="margin-bottom:12px;">
                    <div style="display:flex;justify-content:space-between;font-size:0.8rem;margin-bottom:4px;">
                        <span style="color:#aaa;">💧 古代需水量</span>
                        <span style="color:#60a5fa;font-weight:500;">${water.toFixed(0)} L</span>
                    </div>
                    <div style="display:flex;justify-content:space-between;font-size:0.8rem;margin-bottom:4px;">
                        <span style="color:#aaa;">📐 路线重合度</span>
                        <span style="color:#4ade80;font-weight:500;">${overlap.toFixed(1)}%</span>
                    </div>
                    <div style="height:12px;background:#2a2a4a;border-radius:6px;overflow:hidden;">
                        <div style="height:100%;width:${overlap}%;background:linear-gradient(90deg,#4ade80,#22c55e);"></div>
                    </div>
                </div>
                ${analysis.length ? `
                    <div style="background:#1a1a2e;padding:8px;border-radius:4px;">
                        <div style="font-size:0.75rem;color:#e94560;margin-bottom:4px;">📝 分析</div>
                        <ul style="list-style:none;padding-left:0;margin:0;">
                            ${analysis.map(a => `<li style="padding:3px 0;color:#ccc;font-size:0.8rem;">• ${a}</li>`).join('')}
                        </ul>
                    </div>
                ` : ''}
            </div>
        `;
    },

    async showAllComparisons() {
        const div = document.getElementById('allComparisons');
        div.innerHTML = '<div class="loading">加载中...</div>';
        try {
            const data = await API.getAllRouteComparisons();
            this.allComparisons = Array.isArray(data) ? data : [];
            if (this.allComparisons.length === 0) {
                div.innerHTML = '<div class="empty-state">暂无对比列表</div>';
                return;
            }
            div.innerHTML = `
                <h4 style="font-size:0.85rem;color:#e94560;margin-bottom:10px;padding-bottom:6px;border-bottom:1px solid #2a2a4a;">所有路线对比</h4>
                <div style="max-height:300px;overflow-y:auto;">
                    ${this.allComparisons.slice(0, 10).map((c, i) => this.renderComparisonListItem(c, i)).join('')}
                </div>
            `;
        } catch (error) {
            console.error('加载所有对比失败:', error);
            this.renderFallbackAll();
        }
    },

    renderComparisonListItem(c, index) {
        const ancientDist = c.ancientDistanceKm || c.ancientDistance || 1000 + Math.random() * 1500;
        const modernDist = c.modernDistanceKm || c.modernDistance || ancientDist * 0.7;
        const aName = c.ancientRouteName || c.ancientName || '古代路线 ' + (index + 1);
        const mName = c.modernRoadName || c.modernName || '现代公路 ' + (index + 1);
        const saved = ancientDist ? ((1 - modernDist / ancientDist) * 100) : 0;
        const color = saved > 40 ? '#4ade80' : saved > 20 ? '#fbbf24' : '#ef4444';
        const roadId = c.id || c.roadId || (index + 1);
        const roadDetails = c.roadDetails || this.mockRoadDetails[roadId] || null;
        let badgeHtml = '';
        if (roadDetails && roadDetails.standardCode) {
            const gradeColor = this.roadGradeColors[roadDetails.roadGrade] || '#6b7280';
            badgeHtml = `<span style="padding:1px 6px;border-radius:3px;background:${gradeColor};color:#fff;font-size:0.65rem;font-weight:700;margin-right:6px;">${roadDetails.standardCode}</span>`;
        }
        return `
            <div style="background:#0f172a;padding:10px;border-radius:6px;margin-bottom:8px;cursor:pointer;border:1px solid transparent;transition:all 0.2s;"
                onclick="RouteComparatorPanel.jumpToCompare(${roadId})"
                onmouseover="this.style.borderColor='#e94560';" onmouseout="this.style.borderColor='transparent';">
                <div style="display:flex;align-items:center;margin-bottom:4px;">
                    ${badgeHtml}
                    <div style="font-weight:600;color:#e0e0e0;font-size:0.85rem;flex:1;">${aName} ↔ ${mName}</div>
                </div>
                <div style="display:flex;justify-content:space-between;font-size:0.75rem;color:#aaa;margin-bottom:4px;">
                    <span>🏛️ ${ancientDist.toFixed(0)}km → 🛣️ ${modernDist.toFixed(0)}km</span>
                    <span style="color:${color};font-weight:500;">${saved.toFixed(0)}%</span>
                </div>
                <div style="height:4px;background:#2a2a4a;border-radius:2px;overflow:hidden;">
                    <div style="height:100%;width:${saved}%;background:${color};"></div>
                </div>
            </div>
        `;
    },

    renderFallbackAll() {
        const div = document.getElementById('allComparisons');
        const fallbacks = [
            { a: '长安-敦煌主线', m: 'G30 连霍高速', ad: 1800, md: 1300 },
            { a: '敦煌-楼兰道', m: 'G315 西莎线', ad: 500, md: 420 },
            { a: '楼兰-龟兹道', m: '新疆省道', ad: 600, md: 510 },
            { a: '龟兹-疏勒道', m: 'G314 乌红线', ad: 700, md: 550 }
        ];
        div.innerHTML = `
            <h4 style="font-size:0.85rem;color:#e94560;margin-bottom:10px;padding-bottom:6px;border-bottom:1px solid #2a2a4a;">所有路线对比</h4>
            ${fallbacks.map((f, i) => {
                const saved = ((1 - f.md / f.ad) * 100);
                const color = saved > 30 ? '#4ade80' : saved > 15 ? '#fbbf24' : '#ef4444';
                return `
                    <div style="background:#0f172a;padding:10px;border-radius:6px;margin-bottom:8px;border:1px solid transparent;">
                        <div style="font-weight:600;color:#e0e0e0;font-size:0.85rem;margin-bottom:4px;">${f.a} ↔ ${f.m}</div>
                        <div style="display:flex;justify-content:space-between;font-size:0.75rem;color:#aaa;margin-bottom:4px;">
                            <span>🏛️ ${f.ad}km → 🛣️ ${f.md}km</span>
                            <span style="color:${color};font-weight:500;">${saved.toFixed(0)}%</span>
                        </div>
                        <div style="height:4px;background:#2a2a4a;border-radius:2px;overflow:hidden;">
                            <div style="height:100%;width:${saved}%;background:${color};"></div>
                        </div>
                    </div>
                `;
            }).join('')}
        `;
    },

    jumpToCompare(id) {
        if (document.getElementById('compareModeSelect').value !== 'MODERN') {
            document.getElementById('compareModeSelect').value = 'MODERN';
            document.getElementById('modernRoadSection').classList.remove('hidden');
            document.getElementById('ancientRouteSection').classList.add('hidden');
        }
        const sel = document.getElementById('modernRoadSelect');
        if (sel && sel.querySelector(`option[value="${id}"]`)) {
            sel.value = id;
            this.compareByRoad(id);
        }
    },

    toggleDisplayMode() {
        const modes = ['ANCIENT', 'MODERN', 'BOTH'];
        const idx = modes.indexOf(this.displayMode);
        this.displayMode = modes[(idx + 1) % modes.length];
        if (window.SilkRoadMap && typeof window.SilkRoadMap.setComparisonDisplayMode === 'function') {
            window.SilkRoadMap.setComparisonDisplayMode(this.displayMode);
        }
        const resultDiv = document.getElementById('compareResult');
        if (resultDiv && resultDiv.querySelector('.panel, [style*="background"]')) {
            const labelEl = resultDiv.querySelector('span[style*="font-size:0.7rem"]');
            if (labelEl) labelEl.textContent = '显示模式: ' + this.getDisplayLabel();
        }
    },

    getDisplayLabel() {
        const labels = { ANCIENT: '只古代', MODERN: '只现代', BOTH: '双层叠加' };
        return labels[this.displayMode] || this.displayMode;
    },

    filterRoadsByGrade() {
        const select = document.getElementById('modernRoadSelect');
        if (!select || !this.modernRoads.length) return;
        const filter = this.roadGradeFilter;
        let roads = this.modernRoads;
        if (filter !== 'ALL') {
            roads = this.modernRoads.filter(r => {
                const roadGrade = r.roadGrade || (this.mockRoadDetails[r.id]?.roadGrade);
                return roadGrade === filter;
            });
        }
        if (roads.length === 0) {
            select.innerHTML = '<option value="">暂无符合条件的公路</option>';
        } else {
            select.innerHTML = '<option value="">选择公路</option>' + roads.map(r =>
                `<option value="${r.id || r.roadId}">${r.name || r.roadName || '公路' + r.id} (${(r.distanceKm || 0).toFixed(0)}km)</option>`
            ).join('');
        }
    },

    renderAllComparisonsFiltered() {
        const div = document.getElementById('allComparisons');
        if (!div || !this.allComparisons.length) return;
        let filtered = this.allComparisons;
        if (this.roadGradeFilter !== 'ALL') {
            filtered = this.allComparisons.filter(c => {
                const roadId = c.id || c.roadId;
                const roadDetails = c.roadDetails || this.mockRoadDetails[roadId];
                return roadDetails && roadDetails.roadGrade === this.roadGradeFilter;
            });
        }
        if (filtered.length === 0) {
            div.innerHTML = `
                <h4 style="font-size:0.85rem;color:#e94560;margin-bottom:10px;padding-bottom:6px;border-bottom:1px solid #2a2a4a;">所有路线对比</h4>
                <div class="empty-state">暂无符合条件的路线</div>
            `;
            return;
        }
        div.innerHTML = `
            <h4 style="font-size:0.85rem;color:#e94560;margin-bottom:10px;padding-bottom:6px;border-bottom:1px solid #2a2a4a;">所有路线对比 (${filtered.length}条)</h4>
            <div style="max-height:300px;overflow-y:auto;">
                ${filtered.slice(0, 10).map((c, i) => this.renderComparisonListItem(c, i)).join('')}
            </div>
        `;
    }
};

window.RouteComparatorPanel = RouteComparatorPanel;
