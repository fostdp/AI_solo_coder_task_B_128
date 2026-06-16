-- ============================================================
-- Feature V2: 朝代变迁分析、载重水源优化、古今对比、虚拟旅行
-- 增量脚本，不破坏原有数据
-- ============================================================

-- ============================================================
-- 1. 朝代路线表: 不同历史时期丝绸之路的路线变迁
-- ============================================================
CREATE TABLE IF NOT EXISTS dynasty_routes (
    id SERIAL PRIMARY KEY,
    dynasty VARCHAR(50) NOT NULL,
    dynasty_name VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    name_en VARCHAR(200),
    start_year INTEGER,
    end_year INTEGER,
    geom LINESTRING NOT NULL,
    total_distance_km DOUBLE PRECISION,
    main_commodities TEXT,
    description TEXT,
    political_stability DOUBLE PRECISION DEFAULT 0.5,
    trade_volume_score DOUBLE PRECISION DEFAULT 0.5,
    cultural_exchange_score DOUBLE PRECISION DEFAULT 0.5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_dynasty_routes_geom ON dynasty_routes USING GIST (geom);
CREATE INDEX IF NOT EXISTS idx_dynasty_routes_dynasty ON dynasty_routes (dynasty);

-- 汉代路线 (公元前202年 - 公元220年): 张骞开辟，以长安为起点
INSERT INTO dynasty_routes (dynasty, dynasty_name, name, name_en, start_year, end_year, geom, total_distance_km, main_commodities, description, political_stability, trade_volume_score, cultural_exchange_score) VALUES
('HAN', '汉代', '汉代北道 - 长安至大宛', 'Han Dynasty North Route', -202, 220,
 ST_GeomFromText('LINESTRING(108.94 34.26, 103.83 36.06, 100.45 38.93, 97.14 39.73, 94.66 40.14, 93.51 42.83, 88.23 44.01, 80.03 43.27, 74.58 42.78, 71.67 39.83)', 4326),
 3800, '丝绸、漆器、铜镜; 汗血马、葡萄、苜蓿', '张骞出使西域开辟，汉代是丝绸之路的正式形成期，以军事外交推动贸易', 0.85, 0.7, 0.8),
('HAN', '汉代', '汉代南道 - 敦煌至于阗', 'Han Dynasty South Route', -202, 220,
 ST_GeomFromText('LINESTRING(94.66 40.14, 92.24 40.51, 89.55 40.51, 87.31 40.53, 85.54 39.48, 83.13 38.32, 80.05 36.95, 76.87 39.42, 75.99 39.47)', 4326),
 2200, '丝绸、茶叶; 玉石、香料', '汉代南道沿塔里木盆地南缘西行，经过多个绿洲国家', 0.75, 0.65, 0.7);

-- 唐代路线 (公元618年 - 907年): 盛世丝路，三道并行
INSERT INTO dynasty_routes (dynasty, dynasty_name, name, name_en, start_year, end_year, geom, total_distance_km, main_commodities, description, political_stability, trade_volume_score, cultural_exchange_score) VALUES
('TANG', '唐代', '唐代北道 - 庭州至碎叶', 'Tang Dynasty North Route', 618, 907,
 ST_GeomFromText('LINESTRING(108.94 34.26, 103.83 36.06, 94.66 40.14, 93.51 42.83, 91.62 42.82, 89.18 43.78, 88.23 44.01, 86.18 44.28, 82.61 43.82, 80.03 43.27, 77.03 42.84, 74.58 42.78)', 4326),
 3200, '丝绸、瓷器、茶叶; 金银器、香料、皮毛', '唐代北道经天山北麓至中亚，是最繁忙的丝路通道', 0.9, 0.95, 0.95),
('TANG', '唐代', '唐代中道 - 敦煌至疏勒', 'Tang Dynasty Middle Route', 618, 907,
 ST_GeomFromText('LINESTRING(108.94 34.26, 103.83 36.06, 100.45 38.93, 97.14 39.73, 94.66 40.14, 89.55 40.51, 85.54 39.48, 80.05 36.95, 76.87 39.42, 75.99 39.47, 74.87 40.12)', 4326),
 2800, '丝绸、纸张、铁器; 玉石、玻璃、香料', '唐代中道沿天山南麓，是玄奘取经之路', 0.85, 0.9, 0.9),
('TANG', '唐代', '唐代南道 - 于阗至吐火罗', 'Tang Dynasty South Route', 618, 907,
 ST_GeomFromText('LINESTRING(80.05 36.95, 78.38 37.12, 77.24 38.17, 75.23 39.72, 73.75 39.63, 71.67 39.83, 69.28 40.11, 66.96 39.65, 64.43 39.65, 61.83 36.30)', 4326),
 2600, '丝绸、陶瓷; 胡椒、宝石、药材', '唐代南道翻越帕米尔高原至南亚', 0.7, 0.8, 0.85);

-- 宋代路线 (公元960年 - 1279年): 海上丝路兴起，陆上丝路受阻
INSERT INTO dynasty_routes (dynasty, dynasty_name, name, name_en, start_year, end_year, geom, total_distance_km, main_commodities, description, political_stability, trade_volume_score, cultural_exchange_score) VALUES
('SONG', '宋代', '宋代丝路 - 丝路重心南移', 'Song Dynasty Route', 960, 1279,
 ST_GeomFromText('LINESTRING(108.94 34.26, 103.83 36.06, 100.45 38.93, 97.14 39.73, 94.66 40.14, 89.55 40.51, 85.54 39.48, 80.05 36.95)', 4326),
 2400, '丝绸、瓷器、茶叶; 香料、珠宝、象牙', '宋代因西夏阻隔，陆上丝路衰落，海上丝路兴起', 0.55, 0.45, 0.55);

-- 元代路线 (公元1271年 - 1368年): 蒙古帝国打通欧亚大陆
INSERT INTO dynasty_routes (dynasty, dynasty_name, name, name_en, start_year, end_year, geom, total_distance_km, main_commodities, description, political_stability, trade_volume_score, cultural_exchange_score) VALUES
('YUAN', '元代', '元代草原丝路 - 大都至萨莱', 'Yuan Dynasty Steppe Route', 1271, 1368,
 ST_GeomFromText('LINESTRING(116.40 39.90, 113.65 34.76, 108.94 34.26, 103.83 36.06, 94.66 40.14, 93.51 42.83, 88.23 44.01, 80.03 43.27, 74.58 42.78, 71.67 39.83, 66.96 39.65, 64.43 39.65, 58.35 36.28, 51.42 35.69, 47.24 39.61, 45.40 43.35)', 4326),
 6500, '丝绸、瓷器、罗盘、火药; 金银、皮毛、奴隶', '蒙古帝国时期，欧亚大陆通道被完全打通，马可波罗来华之路', 0.92, 0.9, 0.88),
('YUAN', '元代', '元代绿洲丝路 - 敦煌至波斯', 'Yuan Dynasty Oasis Route', 1271, 1368,
 ST_GeomFromText('LINESTRING(94.66 40.14, 92.24 40.51, 89.55 40.51, 85.54 39.48, 80.05 36.95, 76.87 39.42, 75.99 39.47, 73.75 39.63, 71.67 39.83, 66.96 39.65, 64.43 39.65, 58.35 36.28)', 4326),
 3600, '丝绸、茶叶、纸钞; 香料、宝石、地毯', '元代绿洲丝路仍在使用，商旅不绝', 0.85, 0.8, 0.82);

-- 明代路线 (公元1368年 - 1644年): 闭关锁国，陆上丝路再次衰落
INSERT INTO dynasty_routes (dynasty, dynasty_name, name, name_en, start_year, end_year, geom, total_distance_km, main_commodities, description, political_stability, trade_volume_score, cultural_exchange_score) VALUES
('MING', '明代', '明代丝路 - 嘉峪关以西渐废', 'Ming Dynasty Route', 1368, 1644,
 ST_GeomFromText('LINESTRING(108.94 34.26, 106.16 34.73, 103.83 36.06, 102.64 37.43, 100.45 38.93, 97.14 39.73, 94.66 40.14)', 4326),
 1800, '丝绸、茶叶、瓷器; 马匹、玉石、皮毛', '明朝实行海禁，陆上丝路仅维持至嘉峪关，西域与中原联系减弱', 0.6, 0.4, 0.45);

-- ============================================================
-- 2. 现代公路表: 现代公路与古代丝路对比
-- ============================================================
CREATE TABLE IF NOT EXISTS modern_roads (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    name_en VARCHAR(200),
    road_type VARCHAR(50) NOT NULL,
    geom LINESTRING NOT NULL,
    total_distance_km DOUBLE PRECISION,
    speed_limit_kmh DOUBLE PRECISION DEFAULT 120,
    lane_count INTEGER DEFAULT 4,
    paved BOOLEAN DEFAULT TRUE,
    year_built INTEGER,
    corresponding_ancient_route_id INTEGER REFERENCES routes(id),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_modern_roads_geom ON modern_roads USING GIST (geom);
CREATE INDEX IF NOT EXISTS idx_modern_roads_type ON modern_roads (road_type);

-- 现代公路数据（对应古代丝绸之路的主要路线）
INSERT INTO modern_roads (name, name_en, road_type, geom, total_distance_km, speed_limit_kmh, lane_count, paved, year_built, corresponding_ancient_route_id, description) VALUES
('连霍高速 G30 - 西安至霍尔果斯', 'Lianyungang-Horgos Expressway G30', 'EXPRESSWAY',
 ST_GeomFromText('LINESTRING(108.94 34.26, 106.16 34.73, 103.83 36.06, 102.64 37.43, 100.45 38.93, 97.14 39.73, 94.66 40.14, 93.51 42.83, 88.23 44.01, 86.18 44.28, 82.61 43.82, 80.03 43.27, 77.03 42.84, 74.58 42.78)', 4326),
 3500, 120, 4, TRUE, 2014, 1, '国家高速G30，横贯中国东西，基本沿古代丝绸之路北道，江苏连云港至新疆霍尔果斯口岸，全长4395公里'),
('G315国道 - 西宁至喀什', 'National Highway G315', 'NATIONAL_ROAD',
 ST_GeomFromText('LINESTRING(101.78 36.62, 94.66 40.14, 92.24 40.51, 90.18 40.52, 89.55 40.51, 87.31 40.53, 85.54 39.48, 83.13 38.32, 80.05 36.95, 76.87 39.42, 75.99 39.47)', 4326),
 2800, 80, 2, TRUE, 1953, 3, 'G315国道沿塔里木盆地南缘，经过若羌、且末、于阗等地，与古代南道基本重合'),
('G314国道 - 乌鲁木齐至红其拉甫', 'National Highway G314', 'NATIONAL_ROAD',
 ST_GeomFromText('LINESTRING(86.18 44.28, 88.23 44.01, 89.18 43.78, 91.62 42.82, 93.51 42.83, 94.66 40.14, 97.14 39.73, 100.45 38.93, 76.87 39.42, 75.99 39.47, 75.23 39.72, 74.87 37.82, 74.87 40.12, 75.99 39.47, 73.75 39.63)', 4326),
 2100, 80, 2, TRUE, 1958, 5, 'G314国道连接乌鲁木齐和红其拉甫口岸，穿越天山山脉和帕米尔高原，与古代葱岭道重合'),
('中吉乌国际公路', 'China-Kyrgyzstan-Uzbekistan Highway', 'INTERNATIONAL',
 ST_GeomFromText('LINESTRING(75.99 39.47, 73.75 39.63, 71.67 39.83, 69.28 40.11, 66.96 39.65)', 4326),
 950, 80, 2, TRUE, 2018, 6, '经伊尔克什坦口岸，穿越费尔干纳盆地至撒马尔罕，现代重建的丝路通道'),
('中哈俄跨境公路 - 西安至莫斯科', 'Xi''an-Moscow International Highway', 'INTERNATIONAL',
 ST_GeomFromText('LINESTRING(108.94 34.26, 103.83 36.06, 94.66 40.14, 93.51 42.83, 88.23 44.01, 82.61 43.82, 77.03 42.84, 74.58 42.78, 71.42 51.17, 55.76 37.62, 37.62 55.75)', 4326),
 7500, 100, 2, TRUE, 2010, 9, '新亚欧大陆桥的公路版本，经哈萨克斯坦、俄罗斯至欧洲，古代草原丝路的现代再现');

-- ============================================================
-- 3. 载重-水源消耗配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS cargo_water_configs (
    id SERIAL PRIMARY KEY,
    cargo_type VARCHAR(50) NOT NULL,
    cargo_name VARCHAR(100),
    water_per_100kg_per_day_liters DOUBLE PRECISION DEFAULT 0,
    camel_base_water_daily_l DOUBLE PRECISION DEFAULT 30,
    crew_base_water_daily_l DOUBLE PRECISION DEFAULT 12,
    terrain_factor_desert DOUBLE PRECISION DEFAULT 1.8,
    terrain_factor_mountains DOUBLE PRECISION DEFAULT 1.5,
    terrain_factor_oasis DOUBLE PRECISION DEFAULT 0.8,
    temperature_factor_per_degree DOUBLE PRECISION DEFAULT 0.03,
    max_cargo_per_camel_kg DOUBLE PRECISION DEFAULT 200,
    optimal_cargo_per_camel_kg DOUBLE PRECISION DEFAULT 150,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cargo_config_type ON cargo_water_configs (cargo_type);

INSERT INTO cargo_water_configs (cargo_type, cargo_name, water_per_100kg_per_day_liters, camel_base_water_daily_l, crew_base_water_daily_l, terrain_factor_desert, terrain_factor_mountains, terrain_factor_oasis, temperature_factor_per_degree, max_cargo_per_camel_kg, optimal_cargo_per_camel_kg) VALUES
('SILK', '丝绸', 1.5, 30, 12, 1.8, 1.5, 0.8, 0.03, 200, 150),
('SPICE', '香料', 2.0, 30, 12, 1.8, 1.5, 0.8, 0.03, 200, 150),
('JADE', '玉石', 3.5, 35, 12, 2.0, 1.8, 0.9, 0.03, 250, 180),
('TEA', '茶叶', 1.0, 28, 12, 1.7, 1.4, 0.7, 0.03, 180, 140),
('PORCELAIN', '瓷器', 4.0, 35, 12, 2.0, 1.8, 0.9, 0.03, 250, 180),
('HORSE', '马匹', 0, 45, 12, 1.6, 1.4, 0.9, 0.02, 0, 0),
('GOLD', '金银', 2.5, 32, 15, 1.8, 1.5, 0.8, 0.03, 220, 160),
('GENERAL', '普通货物', 2.0, 30, 12, 1.8, 1.5, 0.8, 0.03, 200, 150);

-- ============================================================
-- 4. 虚拟驼队表
-- ============================================================
CREATE TABLE IF NOT EXISTS virtual_caravans (
    id SERIAL PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    leader_name VARCHAR(50),
    route_id INTEGER REFERENCES routes(id),
    start_waypoint_id INTEGER REFERENCES waypoints(id),
    end_waypoint_id INTEGER REFERENCES waypoints(id),
    current_position POINT,
    current_waypoint_id INTEGER REFERENCES waypoints(id),
    progress_pct DOUBLE PRECISION DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PREPARING',
    speed_kmh DOUBLE PRECISION DEFAULT 5.0,
    cargo_type VARCHAR(50),
    cargo_weight_kg DOUBLE PRECISION DEFAULT 0,
    camel_count INTEGER DEFAULT 10,
    crew_count INTEGER DEFAULT 5,
    water_supply_liters DOUBLE PRECISION DEFAULT 2000,
    water_capacity_liters DOUBLE PRECISION DEFAULT 3000,
    food_supply_days DOUBLE PRECISION DEFAULT 30,
    morale DOUBLE PRECISION DEFAULT 100,
    gold_coins INTEGER DEFAULT 1000,
    distance_traveled_km DOUBLE PRECISION DEFAULT 0,
    journey_days_elapsed INTEGER DEFAULT 0,
    season VARCHAR(20) DEFAULT 'SPRING',
    is_public BOOLEAN DEFAULT FALSE,
    started_at TIMESTAMP,
    last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_virtual_caravans_session ON virtual_caravans (session_id);
CREATE INDEX IF NOT EXISTS idx_virtual_caravans_position ON virtual_caravans USING GIST (current_position);
CREATE INDEX IF NOT EXISTS idx_virtual_caravans_status ON virtual_caravans (status);
CREATE INDEX IF NOT EXISTS idx_virtual_caravans_public ON virtual_caravans (is_public) WHERE is_public = TRUE;

-- ============================================================
-- 5. 虚拟驼队行程事件表
-- ============================================================
CREATE TABLE IF NOT EXISTS caravan_journey_events (
    id SERIAL PRIMARY KEY,
    virtual_caravan_id INTEGER REFERENCES virtual_caravans(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) DEFAULT 'INFO',
    title VARCHAR(200),
    message TEXT,
    geom POINT,
    effect_water_liters DOUBLE PRECISION DEFAULT 0,
    effect_food_days DOUBLE PRECISION DEFAULT 0,
    effect_morale DOUBLE PRECISION DEFAULT 0,
    effect_gold_coins INTEGER DEFAULT 0,
    is_resolved BOOLEAN DEFAULT FALSE,
    event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_journey_events_caravan ON caravan_journey_events (virtual_caravan_id, event_time DESC);
CREATE INDEX IF NOT EXISTS idx_journey_events_type ON caravan_journey_events (event_type);
CREATE INDEX IF NOT EXISTS idx_journey_events_geom ON caravan_journey_events USING GIST (geom);

-- ============================================================
-- 6. 行程发现事件配置 (随机事件库)
-- ============================================================
CREATE TABLE IF NOT EXISTS journey_event_configs (
    id SERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    event_name VARCHAR(100),
    description TEXT,
    terrain_types TEXT,
    min_occurrence_prob DOUBLE PRECISION DEFAULT 0,
    max_occurrence_prob DOUBLE PRECISION DEFAULT 0.1,
    severity VARCHAR(20) DEFAULT 'INFO',
    water_effect_min DOUBLE PRECISION DEFAULT 0,
    water_effect_max DOUBLE PRECISION DEFAULT 0,
    food_effect_min DOUBLE PRECISION DEFAULT 0,
    food_effect_max DOUBLE PRECISION DEFAULT 0,
    morale_effect_min DOUBLE PRECISION DEFAULT 0,
    morale_effect_max DOUBLE PRECISION DEFAULT 0,
    gold_effect_min INTEGER DEFAULT 0,
    gold_effect_max INTEGER DEFAULT 0,
    is_positive BOOLEAN DEFAULT FALSE
);

INSERT INTO journey_event_configs (event_type, event_name, description, terrain_types, min_occurrence_prob, max_occurrence_prob, severity, water_effect_min, water_effect_max, food_effect_min, food_effect_max, morale_effect_min, morale_effect_max, gold_effect_min, gold_effect_max, is_positive) VALUES
('OASIS_DISCOVERED', '发现绿洲', '发现一片未记录的小绿洲，可以补充水源和休整', 'DESERT,DESERT_STEPPE,SAND_DUNES', 0.05, 0.12, 'POSITIVE', 200, 500, 0, 1, 5, 15, 0, 50, TRUE),
('SANDSTORM_ENCOUNTER', '遭遇沙尘暴', '突如其来的沙尘暴侵袭驼队，能见度骤降，行进困难', 'DESERT,SAND_DUNES,DESERT_STEPPE', 0.08, 0.2, 'DANGER', -200, -500, -1, -2, -15, -30, -100, 0, FALSE),
('BANDIT_ATTACK', '遭遇盗匪', '一群盗匪袭击驼队，经过交战后击退了他们，但有所损失', 'DESERT,MOUNTAINS,HIGH_MOUNTAINS', 0.03, 0.08, 'DANGER', -50, -150, -0.5, -1, -20, -35, -300, -50, FALSE),
('LOCAL_MERCHANT', '遇到商人', '路遇友善的本地商人，交易了一些补给', 'OASIS,DESERT_STEPPE,VALLEY', 0.05, 0.1, 'POSITIVE', 0, 100, 0, 2, 5, 10, -50, 0, TRUE),
('WATER_SOURCE_DRY', '水源干涸', '预期的水源已干涸，无法取水', 'DESERT,SAND_DUNES', 0.03, 0.07, 'WARNING', -300, -600, 0, 0, -10, -20, 0, 0, FALSE),
('CAMEL_INJURED', '骆驼受伤', '一头骆驼不慎受伤，行进速度降低', 'MOUNTAINS,HIGH_MOUNTAINS,SAND_DUNES', 0.03, 0.06, 'WARNING', -20, -50, 0, 0, -5, -15, 0, 0, FALSE),
('ANCIENT_RUINS', '发现古迹', '发现了一处被遗忘的古代遗迹，令人振奋', 'DESERT,DESERT_STEPPE,MOUNTAINS', 0.02, 0.05, 'POSITIVE', 0, 0, 0, 0, 10, 20, 50, 200, TRUE),
('HOT_WEATHER', '酷热天气', '异常炎热的天气消耗了大量水分', 'DESERT,SAND_DUNES,DESERT_STEPPE', 0.08, 0.15, 'WARNING', -100, -300, 0, 0, -5, -10, 0, 0, FALSE),
('FRIENDLY_NOMADS', '友好游牧民', '遇到友善的游牧民族，他们赠送了补给', 'STEPPE,DESERT_STEPPE,VALLEY', 0.04, 0.08, 'POSITIVE', 150, 300, 0.5, 1.5, 10, 20, 0, 0, TRUE),
('COLD_WAVE', '寒潮来袭', '气温骤降，寒风刺骨', 'MOUNTAINS,HIGH_MOUNTAINS,STEPPE', 0.05, 0.1, 'WARNING', -30, -80, -0.5, -1.5, -10, -20, 0, 0, FALSE),
('HEAVY_SNOW', '大雪封山', '天降大雪，道路被阻，只能等待或绕行', 'MOUNTAINS,HIGH_MOUNTAINS', 0.02, 0.05, 'DANGER', -50, -150, -1, -3, -15, -25, 0, 0, FALSE),
('PASSENGER_JOIN', '旅行者加入', '一位旅行者请求加入驼队，带来了金币和故事', 'OASIS,VALLEY,DESERT_STEPPE', 0.02, 0.05, 'POSITIVE', -20, 0, -0.5, -1, 5, 10, 100, 300, TRUE);

ANALYZE dynasty_routes;
ANALYZE modern_roads;
ANALYZE cargo_water_configs;
ANALYZE virtual_caravans;
ANALYZE caravan_journey_events;
ANALYZE journey_event_configs;
