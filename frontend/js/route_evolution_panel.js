const RouteEvolutionPanel = {
    container: null,
    dynasties: [],
    currentDynasty: null,
    archaeologicalSites: [],
    historicalDocuments: [],
    showArchaeologyLayer: false,
    evidenceLabels: {
        'CONFIRMED': { text: '已确认', color: '#4ade80' },
        'INFERRED': { text: '推断', color: '#fbbf24' },
        'PROPOSED': { text: '假说', color: '#9ca3af' }
    },
    siteTypeNames: {
        'CITY': '城址',
        'GROTTO': '石窟',
        'STATION': '驿站',
        'PASS': '关隘',
        'TOMB': '墓葬'
    },
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
    mockArchaeologySites: {
        'HAN': [
            { id: 1, name: '敦煌玉门关遗址', type: 'PASS', evidenceStrength: 95, discoveryYear: 1906, description: '汉代西陲重要关隘，现存城墙及烽燧遗址', lng: 97.08, lat: 40.35 },
            { id: 2, name: '楼兰故城遗址', type: 'CITY', evidenceStrength: 88, discoveryYear: 1900, description: '西域楼兰国都城，出土大量汉代文物', lng: 89.95, lat: 40.56 },
            { id: 3, name: '龟兹故城遗址', type: 'CITY', evidenceStrength: 82, discoveryYear: 1958, description: '汉代西域都护府治所之一', lng: 82.96, lat: 41.71 },
            { id: 4, name: '阳关遗址', type: 'PASS', evidenceStrength: 90, discoveryYear: 1943, description: '丝绸之路南道重要关隘', lng: 97.56, lat: 39.93 },
            { id: 5, name: '悬泉置遗址', type: 'STATION', evidenceStrength: 92, discoveryYear: 1987, description: '汉代驿站遗址，出土大量简牍', lng: 95.47, lat: 40.23 }
        ],
        'TANG': [
            { id: 6, name: '大雁塔', type: 'GROTTO', evidenceStrength: 98, discoveryYear: 652, description: '唐代长安著名佛塔，玄奘法师主持修建', lng: 108.96, lat: 34.22 },
            { id: 7, name: '交河故城', type: 'CITY', evidenceStrength: 94, discoveryYear: 1909, description: '唐代西域重要城市，车师前国都城', lng: 89.13, lat: 42.94 },
            { id: 8, name: '高昌故城', type: 'CITY', evidenceStrength: 91, discoveryYear: 1902, description: '唐代高昌国都城，丝路北道重镇', lng: 89.52, lat: 42.87 },
            { id: 9, name: '柏孜克里克千佛洞', type: 'GROTTO', evidenceStrength: 86, discoveryYear: 1879, description: '唐代佛教石窟群，壁画精美', lng: 89.62, lat: 42.96 },
            { id: 10, name: '阿斯塔那古墓群', type: 'TOMB', evidenceStrength: 89, discoveryYear: 1898, description: '唐代高昌居民墓地，出土大量文书', lng: 89.52, lat: 42.89 }
        ],
        'SONG': [
            { id: 11, name: '清明上河园遗址', type: 'CITY', evidenceStrength: 85, discoveryYear: 1981, description: '宋代东京城部分遗址', lng: 114.31, lat: 34.80 },
            { id: 12, name: '泉州刺桐港遗址', type: 'STATION', evidenceStrength: 90, discoveryYear: 1973, description: '宋代海上丝绸之路起点', lng: 118.58, lat: 24.87 }
        ],
        'YUAN': [
            { id: 13, name: '元大都遗址', type: 'CITY', evidenceStrength: 93, discoveryYear: 1964, description: '元代都城遗址，马可波罗曾到访', lng: 116.41, lat: 39.90 },
            { id: 14, name: '马可波罗足迹驿站', type: 'STATION', evidenceStrength: 75, discoveryYear: 1996, description: '推断为马可波罗东行经过的驿站', lng: 108.94, lat: 34.26 }
        ],
        'MING': [
            { id: 15, name: '嘉峪关', type: 'PASS', evidenceStrength: 97, discoveryYear: 1372, description: '明代长城西端起点，丝绸之路咽喉', lng: 98.29, lat: 39.77 },
            { id: 16, name: '莫高窟', type: 'GROTTO', evidenceStrength: 99, discoveryYear: 1900, description: '明代仍有修缮的佛教艺术宝库', lng: 94.81, lat: 40.04 }
        ]
    },
    mockDocuments: {
        'HAN': [
            { id: 1, title: '史记·大宛列传', author: '司马迁', reliability: 95, excerpt: '大宛在匈奴西南，在汉正西，去汉可万里。其俗土著，耕田，田稻麦。有蒲陶酒。' },
            { id: 2, title: '汉书·西域传', author: '班固', reliability: 92, excerpt: '西域以孝武时始通，本三十六国，其后稍分至五十余，皆在匈奴之西，乌孙之南。' },
            { id: 3, title: '张骞出使西域记', author: '张骞', reliability: 88, excerpt: '骞身所至者大宛、大月氏、大夏、康居，而传闻其旁大国五六。' }
        ],
        'TANG': [
            { id: 4, title: '大唐西域记', author: '玄奘', reliability: 96, excerpt: '亲践者一百一十国，传闻者二十八国，虽山川阻险，而风俗可详。' },
            { id: 5, title: '旧唐书·西戎传', author: '刘昫', reliability: 90, excerpt: '贞观四年，李靖击灭突厥，伊吾城主石万年率七城内附，置西伊州。' },
            { id: 6, title: '经行记', author: '杜环', reliability: 85, excerpt: '环随镇西节度使高仙芝西征，天宝十载至西海，宝应初因贾商船舶自广州而回。' }
        ],
        'SONG': [
            { id: 7, title: '诸蕃志', author: '赵汝适', reliability: 88, excerpt: '大秦国一名犁靬，在海西，地方数千里，有四百余城。' },
            { id: 8, title: '岭外代答', author: '周去非', reliability: 82, excerpt: '诸蕃国之富盛多宝货者，莫如大食国。' }
        ],
        'YUAN': [
            { id: 9, title: '马可波罗行纪', author: '马可波罗', reliability: 78, excerpt: '契丹为世界最富之国，商货宝石，输入无数。' },
            { id: 10, title: '元史·地理志', author: '宋濂', reliability: 90, excerpt: '西北皆不用，而西域之道四通，驿传之盛，前代未有也。' }
        ],
        'MING': [
            { id: 11, title: '西域番国志', author: '陈诚', reliability: 92, excerpt: '永乐十三年，吏部员外郎陈诚使西域，历哈密、吐鲁番诸地。' },
            { id: 12, title: '三宝太监西洋记', author: '罗懋登', reliability: 70, excerpt: '永乐七年，郑和统领舟师往诸番国，开读赏赐。' }
        ]
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
                <div class="form-group checkbox-group">
                    <input type="checkbox" id="showArchaeologySites">
                    <label for="showArchaeologySites">显示考古遗址图层</label>
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
                <div id="archaeologySection" style="margin-top:15px;display:none;"></div>
                <div id="documentsSection" style="margin-top:15px;display:none;"></div>
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
            this.hideArchaeologyAndDocuments();
        });
        document.getElementById('showArchaeologySites').addEventListener('change', (e) => {
            this.showArchaeologyLayer = e.target.checked;
            this.toggleArchaeologyLayer();
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
            const routesWithEvidence = routes.map(r => {
                const evidence = r.evidenceLevel || (r.distanceKm > 1000 ? 'CONFIRMED' : r.distanceKm > 500 ? 'INFERRED' : 'PROPOSED');
                return { ...r, evidenceLevel: evidence };
            });
            if (window.SilkRoadMap && typeof window.SilkRoadMap.showDynastyRoutes === 'function') {
                window.SilkRoadMap.showDynastyRoutes(routesWithEvidence, dynasty, color);
            }
            this.loadArchaeologyData(dynasty);
            this.loadDocumentsData(dynasty);
            if (this.showArchaeologyLayer) {
                this.toggleArchaeologyLayer();
            }
        } catch (error) {
            console.error('加载朝代路线失败:', error);
            this.archaeologicalSites = this.mockArchaeologySites[dynasty] || [];
            this.historicalDocuments = this.mockDocuments[dynasty] || [];
            this.renderArchaeologySection();
            this.renderDocumentsSection();
            if (this.showArchaeologyLayer) {
                this.toggleArchaeologyLayer();
            }
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
    },

    async loadArchaeologyData(dynasty) {
        try {
            const data = await API.getArchaeologicalSites(dynasty);
            this.archaeologicalSites = Array.isArray(data) ? data : [];
        } catch (error) {
            console.error('加载考古遗址失败:', error);
            this.archaeologicalSites = this.mockArchaeologySites[dynasty] || [];
        }
        this.renderArchaeologySection();
    },

    async loadDocumentsData(dynasty) {
        try {
            const data = await API.getHistoricalDocuments(dynasty);
            this.historicalDocuments = Array.isArray(data) ? data : [];
        } catch (error) {
            console.error('加载历史文献失败:', error);
            this.historicalDocuments = this.mockDocuments[dynasty] || [];
        }
        this.renderDocumentsSection();
    },

    renderArchaeologySection() {
        const div = document.getElementById('archaeologySection');
        if (!div) return;
        const sites = this.archaeologicalSites || [];
        if (sites.length === 0) {
            div.style.display = 'none';
            return;
        }
        div.style.display = 'block';
        const dynastyName = this.dynastyNames[this.currentDynasty] || this.currentDynasty;
        div.innerHTML = `
            <div style="background:#0f172a;padding:12px;border-radius:6px;border-top:3px solid #10b981;">
                <div style="display:flex;justify-content:space-between;align-items:center;cursor:pointer;" onclick="RouteEvolutionPanel.toggleSection('archaeology')">
                    <h4 style="color:#10b981;font-size:0.9rem;margin:0;">🏺 考古证据 <span style="font-size:0.75rem;color:#888;">(${sites.length}处遗址)</span></h4>
                    <span id="archaeologyToggleIcon" style="color:#888;font-size:0.8rem;">▼</span>
                </div>
                <div id="archaeologyContent" style="margin-top:10px;">
                    ${sites.map(s => this.renderSiteCard(s)).join('')}
                </div>
            </div>
        `;
    },

    renderSiteCard(site) {
        const typeName = this.siteTypeNames[site.type] || site.type || '遗址';
        const strength = site.evidenceStrength || 0;
        const strengthColor = strength >= 85 ? '#4ade80' : strength >= 60 ? '#fbbf24' : '#ef4444';
        const typeIcons = {
            'CITY': '🏯', 'GROTTO': '🛕', 'STATION': '📯', 'PASS': '🏰', 'TOMB': '⚱️'
        };
        const icon = typeIcons[site.type] || '🏛️';
        return `
            <div style="padding:10px;background:#1a1a2e;border-radius:5px;margin-bottom:8px;">
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">
                    <div style="display:flex;align-items:center;gap:6px;">
                        <span style="font-size:1.1rem;">${icon}</span>
                        <span style="font-weight:600;color:#e0e0e0;font-size:0.85rem;">${site.name}</span>
                    </div>
                    <span style="font-size:0.7rem;padding:2px 6px;border-radius:8px;background:${strengthColor}20;color:${strengthColor};border:1px solid ${strengthColor};">
                        ${typeName}
                    </span>
                </div>
                <div style="margin-bottom:6px;">
                    <div style="display:flex;justify-content:space-between;font-size:0.7rem;margin-bottom:2px;">
                        <span style="color:#aaa;">证据强度</span>
                        <span style="color:${strengthColor};font-weight:500;">${strength}%</span>
                    </div>
                    <div style="height:6px;background:#2a2a4a;border-radius:3px;overflow:hidden;">
                        <div style="height:100%;width:${strength}%;background:${strengthColor};"></div>
                    </div>
                </div>
                <div style="display:flex;justify-content:space-between;font-size:0.75rem;color:#888;margin-bottom:4px;">
                    <span>📅 发现年份: ${site.discoveryYear || '-'}</span>
                </div>
                <div style="font-size:0.75rem;color:#aaa;line-height:1.4;">${site.description || ''}</div>
            </div>
        `;
    },

    renderDocumentsSection() {
        const div = document.getElementById('documentsSection');
        if (!div) return;
        const docs = this.historicalDocuments || [];
        if (docs.length === 0) {
            div.style.display = 'none';
            return;
        }
        div.style.display = 'block';
        div.innerHTML = `
            <div style="background:#0f172a;padding:12px;border-radius:6px;border-top:3px solid #a855f7;">
                <div style="display:flex;justify-content:space-between;align-items:center;cursor:pointer;" onclick="RouteEvolutionPanel.toggleSection('documents')">
                    <h4 style="color:#a855f7;font-size:0.9rem;margin:0;">📜 历史文献 <span style="font-size:0.75rem;color:#888;">(${docs.length}篇)</span></h4>
                    <span id="documentsToggleIcon" style="color:#888;font-size:0.8rem;">▼</span>
                </div>
                <div id="documentsContent" style="margin-top:10px;">
                    ${docs.map(d => this.renderDocumentCard(d)).join('')}
                </div>
            </div>
        `;
    },

    renderDocumentCard(doc) {
        const reliability = doc.reliability || 0;
        const relColor = reliability >= 90 ? '#4ade80' : reliability >= 75 ? '#fbbf24' : '#ef4444';
        return `
            <div style="padding:10px;background:#1a1a2e;border-radius:5px;margin-bottom:8px;">
                <div style="display:flex;justify-content:space-between;align-items:start;margin-bottom:6px;">
                    <div>
                        <div style="font-weight:600;color:#e0e0e0;font-size:0.85rem;margin-bottom:2px;">${doc.title}</div>
                        <div style="font-size:0.75rem;color:#888;">👤 ${doc.author || '佚名'}</div>
                    </div>
                    <div style="text-align:right;">
                        <div style="font-size:0.7rem;color:#aaa;margin-bottom:2px;">可靠度</div>
                        <div style="font-weight:600;color:${relColor};font-size:0.9rem;">${reliability}%</div>
                    </div>
                </div>
                <div style="padding:8px;background:#0f172a;border-left:3px solid ${relColor};border-radius:0 4px 4px 0;">
                    <div style="font-size:0.75rem;color:#ccc;line-height:1.5;font-style:italic;">"${doc.excerpt || ''}"</div>
                </div>
            </div>
        `;
    },

    toggleSection(section) {
        const content = document.getElementById(section + 'Content');
        const icon = document.getElementById(section + 'ToggleIcon');
        if (!content || !icon) return;
        if (content.style.display === 'none') {
            content.style.display = '';
            icon.textContent = '▼';
        } else {
            content.style.display = 'none';
            icon.textContent = '▶';
        }
    },

    toggleArchaeologyLayer() {
        if (!window.SilkRoadMap) return;
        if (this.showArchaeologyLayer && this.archaeologicalSites.length > 0) {
            if (typeof window.SilkRoadMap.showArchaeologicalSites === 'function') {
                window.SilkRoadMap.showArchaeologicalSites(this.archaeologicalSites);
            }
        } else {
            if (typeof window.SilkRoadMap.clearArchaeologicalSites === 'function') {
                window.SilkRoadMap.clearArchaeologicalSites();
            }
        }
    },

    hideArchaeologyAndDocuments() {
        const archDiv = document.getElementById('archaeologySection');
        const docDiv = document.getElementById('documentsSection');
        if (archDiv) archDiv.style.display = 'none';
        if (docDiv) docDiv.style.display = 'none';
        this.archaeologicalSites = [];
        this.historicalDocuments = [];
        if (window.SilkRoadMap && typeof window.SilkRoadMap.clearArchaeologicalSites === 'function') {
            window.SilkRoadMap.clearArchaeologicalSites();
        }
        const checkbox = document.getElementById('showArchaeologySites');
        if (checkbox) checkbox.checked = false;
        this.showArchaeologyLayer = false;
    }
};

window.RouteEvolutionPanel = RouteEvolutionPanel;