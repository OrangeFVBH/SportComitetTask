from flask import Flask, jsonify, request
import sqlite3

app = Flask(__name__)


def get_db_connection():
    conn = sqlite3.connect('sport.db')
    conn.row_factory = sqlite3.Row  # Это позволит обращаться к столбцам по имени
    return conn


# Создание таблиц и их инициализация при запуске
def init_db():
    conn = get_db_connection()
    cursor = conn.cursor()  # Используем курсор для выполнения команд

    # Таблица секций
    cursor.execute('''CREATE TABLE IF NOT EXISTS sections 
        (id INTEGER PRIMARY KEY AUTOINCREMENT, org_name TEXT, leader TEXT, 
         address TEXT, phone TEXT, city TEXT, sports TEXT, schedule TEXT, age_groups TEXT)''')

    # Таблица пользователей
    cursor.execute('''CREATE TABLE IF NOT EXISTS users 
        (id INTEGER PRIMARY KEY AUTOINCREMENT, login TEXT UNIQUE, password TEXT, role TEXT)''')

    # Таблица статистики (кликов)
    cursor.execute('''CREATE TABLE IF NOT EXISTS stats (sport_name TEXT UNIQUE, count INTEGER DEFAULT 0)''')

    # Добавим начальные данные для секций, если таблица пуста
    cursor.execute("SELECT COUNT(*) FROM sections")
    if cursor.fetchone()[0] == 0:  # Если таблица пуста
        initial_sections_data = [
            {"org_name": "СШОР №1", "leader": "Иванов Иван Иванович", "address": "ул. Ленина, 15",
             "phone": "89001112233", "city": "Стерлитамак", "sports": "Бокс;Футбол;Дзюдо",
             "schedule": "Пн-Пт 09:00-18:00", "age_groups": "7-14 лет"},
            {"org_name": "Олимп", "leader": "Кузнецов Олег Игоревич", "address": "ул. Артема, 3",
             "phone": "89605556677", "city": "Стерлитамак", "sports": "Баскетбол;Теннис",
             "schedule": "Ежедневно 08:00-21:00", "age_groups": "от 6 лет и старше"}
        ]
        for section in initial_sections_data:
            cursor.execute('''INSERT INTO sections 
                            (org_name, leader, address, phone, city, sports, schedule, age_groups) 
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)''',
                           (section['org_name'], section['leader'], section['address'], section['phone'],
                            # !!! ИЗМЕНЕНИЕ: Сохраняем city и sports в нижнем регистре !!!
                            section['city'].lower(), section['sports'].lower(), section['schedule'],
                            section['age_groups']))
        print("Initial sections data added.")
    else:
        print("Sections table already contains data. Skipping initial insert.")

    # Добавим тестовых пользователей, если их нет (используем INSERT OR IGNORE)
    initial_users_data = [
        ('admin', 'admin', 'admin'),
        ('coach_ivan', 'pass123', 'admin'),
        ('user1', '1111', 'user'),
        ('student2024', 'qwerty', 'user')
    ]
    for login, password, role in initial_users_data:
        try:
            cursor.execute("INSERT INTO users (login, password, role) VALUES (?, ?, ?)", (login, password, role))
            print(f"User {login} added.")
        except sqlite3.IntegrityError:
            print(f"User {login} already exists. Skipping.")
            pass  # Пользователь уже существует, игнорируем ошибку

    conn.commit()
    conn.close()


init_db()


@app.after_request
def add_header(response):
    response.headers['ngrok-skip-browser-warning'] = 'true'
    return response


# --- API ЭНДПОИНТЫ ---

@app.route('/api/login', methods=['POST'])
def login():
    data = request.json
    conn = get_db_connection()
    user = conn.execute('SELECT * FROM users WHERE login = ? AND password = ?',
                        (data['login'], data['password'])).fetchone()
    conn.close()
    if user:
        return jsonify({"status": "success", "role": user['role']})
    return jsonify({"status": "error", "message": "Неверный логин или пароль"}), 401


@app.route('/api/sports', methods=['GET'])
def get_sports():
    conn = get_db_connection()
    sections = conn.execute('SELECT sports FROM sections').fetchall()
    conn.close()
    all_sports = set()
    for row in sections:
        if row['sports']:  # Убедимся, что row['sports'] не None
            for s in str(row['sports']).split(';'):
                # !!! ИЗМЕНЕНИЕ: capitalize() для отображения с большой буквы, но хранится в базе в нижнем !!!
                all_sports.add(s.strip().capitalize())
    return jsonify(sorted(list(all_sports)))


@app.route('/api/search', methods=['GET'])
def search_sections():
    # Очищаем входные данные от пробелов и приводим к нижнему регистру
    city_query = request.args.get('city', '').lower().replace(" ", "")
    sport_query = request.args.get('sport', '').lower().strip()  # Добавлено .strip()

    print(f"DEBUG (Server): Received city_query='{city_query}', sport_query='{sport_query}'")

    conn = get_db_connection()

    # Обновление статистики клика (увеличиваем счетчик)
    if sport_query:
        conn.execute("INSERT INTO stats (sport_name, count) VALUES (?, 1) "
                     "ON CONFLICT(sport_name) DO UPDATE SET count = count + 1",
                     (sport_query,))
        conn.commit()  # Commit статистики сразу после обновления

    # --- Динамическое построение SQL-запроса ---
    sql_parts = []
    query_params = []

    # Добавляем условие по городу, только если city_query не пуст
    if city_query:
        # !!! ИЗМЕНЕНИЕ: Убран lower() из SQL, так как city уже хранится в нижнем регистре !!!
        sql_parts.append("replace(city, ' ', '') = ?")
        query_params.append(city_query)

    # Добавляем условие по виду спорта, только если sport_query не пуст
    # !!! ИЗМЕНЕНИЕ: Убран lower() из SQL, так как sports уже хранится в нижнем регистре !!!
    if sport_query:
        sql_parts.append("sports LIKE ?")
        query_params.append(f"%{sport_query}%")

    # Формируем окончательный SQL-запрос
    final_sql_query = "SELECT * FROM sections"
    if sql_parts:
        final_sql_query += " WHERE " + " AND ".join(sql_parts)

    print(f"DEBUG (Server): SQL query to execute: '{final_sql_query}' with params: {query_params}")

    try:
        raw_sections = conn.execute(final_sql_query, tuple(query_params)).fetchall()
        print(f"DEBUG (Server): Found {len(raw_sections)} sections from DB before final filtering.")
    except Exception as e:
        print(f"ERROR (Server): SQL execution failed: {e}")
        raw_sections = []
    finally:
        conn.close()

    results = []
    for row in raw_sections:
        d = dict(row)  # Преобразуем Row в обычный словарь

        # !!! ИЗМЕНЕНИЕ: При отображении видов спорта в приложении,
        # делаем их с большой буквы для красоты, но в базе они в нижнем.
        if d.get('sports'):
            d['sports'] = [s.strip().capitalize() for s in str(d['sports']).split(';')]
        else:
            d['sports'] = []
        results.append(d)
        print(f"DEBUG (Server): Section ID={d.get('id', 'N/A')} ('{d.get('org_name', 'N/A')}') added to results.")

    print(f"DEBUG (Server): Final results count: {len(results)}")
    return jsonify(results)


@app.route('/api/admin/add', methods=['POST'])
def add_section():
    data = request.json
    required_fields = ['org_name', 'leader', 'address', 'phone', 'city', 'sports', 'schedule', 'age_groups']
    if not all(field in data for field in required_fields):
        return jsonify({"status": "error", "message": "Отсутствуют обязательные поля"}), 400

    conn = get_db_connection()
    try:
        cursor = conn.cursor()
        cursor.execute('''INSERT INTO sections (org_name, leader, address, phone, city, sports, schedule, age_groups) 
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)''',
                     (data['org_name'], data['leader'], data['address'], data['phone'],
                      data['city'].lower(), data['sports'].lower(), data['schedule'], data['age_groups']))
        new_id = cursor.lastrowid # Получаем ID последней вставленной строки
        conn.commit()
        conn.close()
        return jsonify({"status": "added", "id": new_id})
    except Exception as e:
        if conn: conn.close()
        return jsonify({"status": "error", "message": str(e)}), 500


@app.route('/api/admin/stats', methods=['GET'])
def get_stats():
    conn = get_db_connection()
    try:
        # Получаем данные, гарантируя, что count — это число
        rows = conn.execute("SELECT sport_name, count FROM stats WHERE count > 0 ORDER BY count DESC").fetchall()
        stats = [{"label": row['sport_name'].capitalize(), "value": float(row['count'])} for row in rows]
        return jsonify(stats)
    except Exception as e:
        print(f"Ошибка статистики: {e}")
        return jsonify([]) # Возвращаем пустой список вместо ошибки 500
    finally:
        conn.close()

@app.route('/api/register', methods=['POST'])
def register():
    data = request.json
    login = data.get('login')
    password = data.get('password')

    if not login or not password:
        return jsonify({"status": "error", "message": "Логин и пароль обязательны"}), 400

    role = 'user'  # Новые пользователи регистрируются как обычные юзеры

    conn = get_db_connection()
    try:
        existing_user = conn.execute('SELECT id FROM users WHERE login = ?', (login,)).fetchone()
        if existing_user:
            conn.close()
            return jsonify({"status": "error", "message": "Пользователь с таким логином уже существует"}), 409

        conn.execute('INSERT INTO users (login, password, role) VALUES (?, ?, ?)',
                     (login, password, role))
        conn.commit()
        conn.close()
        return jsonify({"status": "success", "message": "Пользователь успешно зарегистрирован"})
    except Exception as e:
        conn.close()
        return jsonify({"status": "error", "message": f"Ошибка регистрации: {str(e)}"}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
