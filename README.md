# 📊 DeeployRKD — Система обліку продажів та аналітичної звітності

> **DeeployRKD** — повнофункціональний корпоративний веб-застосунок для моніторингу продажів, формування аналітичних звітів у форматах **PDF** (з графіками) та **Excel**, а також автоматичної розсилки звітів на Email. Проєкт повністю контейнеризований та адаптований для розгортання на спільних серверах за допомогою Docker Compose та Nginx.

---

## 🚀 Основні можливості

- **Управління продажами (CRUD)**: додавання, перегляд, видалення записів про продажі із прив'язкою до менеджерів, регіонів та дат.
- **Аналітика та фільтрація**: швидка вибірка даних за часовими інтервалами (місяць, період від/до) та регіонами (Київ, Західний, Центральний, Південний, Східний).
- **Генерація PDF-звітів**: побудова структурованих PDF-документів із підтримкою кирилиці (DejaVuSans) та вбудованими стовпчастими діаграмами (JFreeChart).
- **Експорт у Excel (.xlsx)**: формування таблиць Microsoft Excel засобами Apache POI із автоматичним підрахунком підсумків.
- **Email-розсилка**: відправка згенерованих звітів безпосередньо на пошту (SMTP / Gmail) через фоновий сервіс.
- **Гібридне сховище даних**: інтелектуальне перемикання між **PostgreSQL 16** та **InMemory** сховищем (за відсутності з'єднання з БД застосунок продовжує роботу без збоїв).
- **Зворотний проксі Nginx**: безпечна маршрутизація, захист вихідних портів та підтримка сучасних Security Headers (`X-Frame-Options`, `nosniff`, `Referrer-Policy`, `Permissions-Policy`).

---

## 🛠 Стек технологій

| Сфера | Технології |
|---|---|
| **Backend** | Java 17, Spring Boot 3.4 (Web, JDBC, Mail, Validation), HikariCP |
| **Звітність & Графіка** | OpenPDF 1.3.40, JFreeChart 1.5.5, Apache POI 5.3.0 (OOXML) |
| **Frontend** | React 18, Vite, Tailwind CSS, Lucide Icons |
| **База даних** | PostgreSQL 16 (Alpine) / InMemory ConcurrentHashMap |
| **Контейнеризація** | Docker (Multi-stage build), Docker Compose v2 |
| **Веб-сервер / Proxy** | Nginx Reverse Proxy, UFW Firewall |

---

## 📁 Структура проєкту

```text
.
├── backend/                        # Spring Boot бекенд
│   ├── src/main/java/              # Вихідний код Java (контролери, сервіси, репозиторії)
│   ├── src/main/resources/         # application.yaml, шрифти DejaVu, статичні ресурси
│   ├── Dockerfile                  # Оптимізований Multi-stage Dockerfile бекенду
│   ├── .dockerignore               # Правила виключення файлів для Docker
│   └── pom.xml                     # Залежності Maven
├── frontend/                       # React + Vite клієнтська частина
│   ├── src/                        # Компоненти інтерфейсу (App.jsx, Tailwind стилі)
│   ├── package.json                # Залежності фронтенду
│   └── vite.config.js              # Конфігурація збірки Vite
├── nginx/                          # Конфігурації Nginx Reverse Proxy
│   └── drachenko.conf              # Блок віртуального хоста із проксі-заголовками
├── docker-compose.yml              # Стек сервісів (Server + PostgreSQL + Healthcheck + Volumes)
├── PORTS.md                        # Карта непересічних портів для спільної VM (OFFSET = index * 10)
├── .env.example                    # Шаблон змінних оточення
├── .gitignore                      # Оптимізовані правила ігнорування Git
└── README.md                       # Документація проєкту
```

---

## 🌐 Планування портів (`PORTS.md`)

Для розгортання на спільній віртуальній машині кількома розробниками використовується формула зміщення:
$$\text{OFFSET} = \text{index} \times 10$$

| Сервіс | Змінна в `.env` | Студент 0 (Default) | Студент 1 | Студент 2 |
|---|---|---|---|---|
| **Backend API** | `API_PORT` | `5000` | `5010` | `5020` |
| **База даних (Internal)** | `DB_PORT` | `5432` | `5442` | `5452` |
| **Nginx (Public Web)** | `WEB_PORT` | `8080` | `8090` | `8100` |

---

## ⚙️ Налаштування оточення (`.env`)

Створіть локальний файл `.env` на основі `.env.example`:

```bash
cp .env.example .env
```

Приклад вмісту `.env`:
```ini
# Порти (відповідно до вашого індексу в PORTS.md)
WEB_PORT=8080
API_PORT=5000

# Параметри PostgreSQL
DB_HOST=db
DB_PORT=5432
DB_NAME=drachenko_app
DB_USERNAME=drachenko_user
DB_PASSWORD=your_secure_password

# Налаштування пошти (Spring Mail SMTP)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true

# Отримувачі звітів за замовчуванням
APP_REPORTS_DEFAULT_EMAILS=ceo@example.com
```

---

## 🚀 Інструкція із запуску та підняття проєкту

### Спосіб 1: Швидкий запуск через Docker Compose (Рекомендований)

Цей спосіб автоматично збирає бекенд, створює PostgreSQL базу даних, запускає `healthcheck` і піднімає весь стек однією командою.

#### Крок 1. Підготовка файлу змінних оточення
Створіть файл `.env` із шаблону (якщо його ще немає):
```bash
cp .env.example .env
```
*(Переконайтеся, що в `.env` вказані ваші порти та параметри, наприклад `API_PORT=5000`, `DB_PORT=5432`, `DB_NAME=drachenko_app`)*.

#### Крок 2. Збірка та запуск контейнерів
```bash
docker compose up -d --build
```
> Прапорець `-d` запускає контейнери у фоновому режимі (detached), а `--build` перезбирає образи за наявності змін у коді.

#### Крок 3. Перевірка статусу сервісів
```bash
docker compose ps
```
Ви повинні побачити обидва сервіси:
- `drachenko-db` — статус `Up (healthy)`
- `drachenko-server` — статус `Up`

#### Крок 4. Доступ до застосунку
- **API та вбудований інтерфейс**: [http://localhost:5000](http://localhost:5000) *(або `http://<IP_сервера>:5000`)*
- **Перевірка роботи через термінал**:
  ```bash
  curl http://localhost:5000/sales
  ```

---

### Спосіб 2: Запуск із Nginx Reverse Proxy (Production / Спільна VM)

Використовується для розгортання на сервері або спільній віртуальній машині відповідно до Занять 2-4 курсу.

1. **Підніміть стек Docker Compose** (як описано у Способі 1):
   ```bash
   docker compose up -d --build
   ```

2. **Встановіть Nginx** (якщо не встановлено):
   ```bash
   sudo apt update && sudo apt install -y nginx
   ```

3. **Підключіть конфігурацію проєкту**:
   ```bash
   # Копіюємо конфіг у доступні сайти Nginx
   sudo cp nginx/drachenko.conf /etc/nginx/sites-available/drachenko

   # Активуємо сайт через створення симлінка
   sudo ln -s /etc/nginx/sites-available/drachenko /etc/nginx/sites-enabled/drachenko

   # Перевіряємо валідність синтаксису
   sudo nginx -t

   # Перезавантажуємо Nginx
   sudo systemctl reload nginx
   ```

4. **Налаштуйте фаєрвол (UFW)** для закриття прямого доступу:
   ```bash
   sudo ufw allow 8080/tcp    # Дозволяємо публічний веб-порт Nginx
   sudo ufw deny 5000/tcp     # Блокуємо прямий зовнішній доступ до сирого порту бекенду
   ```

5. **Перевірка доступу через Nginx**:
   ```bash
   curl -I http://localhost:8080
   ```

---

### Спосіб 3: Локальна розробка без Docker (Hot-Reload)

Якщо ви розробляєте нові фічі і потребуєте миттєвого оновлення коду:

#### 1. Запуск бекенду (Spring Boot):
```bash
cd backend
./mvnw spring-boot:run
```
*Застосунок підніметься на `http://localhost:8080` (або `http://localhost:5000` за наявності `SERVER_PORT` у `.env`). Якщо PostgreSQL не запущено, бекенд автоматично перемкнеться на внутрішнє InMemory сховище.*

#### 2. Запуск фронтенду (React + Vite):
```bash
cd frontend
npm install
npm run dev
```
*Інтерфейс відкриється на `http://localhost:5173` із підтримкою Fast Refresh.*

---

## 🛠️ Корисні команди обслуговування

| Дія | Команда |
|---|---|
| **Перегляд логів усіх сервісів** | `docker compose logs -f` |
| **Перегляд логів лише бекенду** | `docker compose logs -f server` |
| **Перегляд логів бази даних** | `docker compose logs -f db` |
| **Перезапуск сервісу без перезбірки** | `docker compose restart server` |
| **Зупинка контейнерів зі збереженням БД** | `docker compose down` |
| **Повна зупинка з очищенням томів/БД** | `docker compose down -v` |
| **Очищення завислих контейнерів** | `docker rm -f drachenko-server drachenko-db` |

---

## ❓ Часті проблеми та їх вирішення (Troubleshooting)

- **Помилка `Conflict. The container name is already in use`**:
  Попередній контейнер не був коректно видалений. Виконайте:
  ```bash
  docker rm -f drachenko-server drachenko-db
  ```
- **Помилка `port is already allocated`**:
  Порт зайнятий іншим процесом на машині. Перевірте змінні `API_PORT` та `WEB_PORT` у вашому `.env` та оберіть вільні порти згідно з `PORTS.md`.
- **Помилка з'єднання з БД у бекенді**:
  Переконайтеся, що `drachenko-db` пройшов healthcheck (`docker compose ps` показує `(healthy)`), а `DB_HOST=db` у файлі `.env`.

---

## 📡 REST API Документація

| Метод | Ендпоінт | Опис | Параметри |
|---|---|---|---|
| `GET` | `/sales` | Отримати список усіх продажів | `region`, `month`, `from`, `to` |
| `POST` | `/sales` | Створити новий запис продажу | JSON Body (`SaleRequest`) |
| `GET` | `/sales/{id}` | Отримати продаж за ID | `id` (шлях) |
| `DELETE`| `/sales/{id}` | Видалити продаж за ID | `id` (шлях) |
| `GET` | `/reports/summary` | Отримати зведену статистику | `region`, `month`, `from`, `to` |
| `GET` | `/reports/sales.pdf` | Завантажити звіт у форматі **PDF** | `region`, `month`, `from`, `to` |
| `GET` | `/reports/sales.xlsx`| Завантажити звіт у форматі **Excel** | `region`, `month`, `from`, `to` |
| `POST` | `/reports/send` | Надіслати звіт на Email | JSON Body (`EmailReportRequest`) або query params |

