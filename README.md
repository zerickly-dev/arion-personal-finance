# 💸 Arion

**Arion** es una app de escritorio hecha en **JavaFX** para gestionar tus finanzas personales.  
La idea es simple: llevar un control de tus ingresos y gastos de manera rápida y visual. 🚀

## ✨ Features
- 👤 Registro e inicio de sesión de usuarios
- 💰 Agregar, editar y eliminar transacciones
- 🏷️ Categorías de ingresos y gastos
- 📊 Vista clara de tus movimientos
- 🗄️ Base de datos en **PostgreSQL**
- 🎨 UI en JavaFX + FXML + CSS
- 📩 Exportar a PDF

## 🛠️ Tech Stack
- Java 21
- JavaFX
- Maven
- PostgreSQL
- Ikonli
- OpenPDF

## ⚡ Requisitos
- ☕ [Java 17+](https://adoptium.net/)
- 🐘 [PostgreSQL](https://www.postgresql.org/)
- 🛠️ [Maven](https://maven.apache.org/)

## 🚀 Run it
Clona el repo y corre con Maven:
```bash
git clone https://github.com/ericklara-dev/arion-personal-finance.git
cd arion
mvn clean install
mvn javafx:run
```


⚙️ Antes de arrancar, crea un archivo config.properties en src/main/resources/:
```properties
db.url=jdbc:postgresql://localhost:5432/arion
db.user=tu_usuario
db.password=tu_password
```
🗄️ Database

Tablas principales:
```sql
users (id, username, email, password)

transactions (id, user_id, description, category, date, amount, type, note)
```
📷 Screenshots

<img width="1279" height="721" alt="Captura de pantalla 2025-09-07 133129" src="https://github.com/user-attachments/assets/6ed54983-f614-473d-a33b-2df06cd4d000" />

<img width="1086" height="755" alt="image" src="https://github.com/user-attachments/assets/5b132155-56f6-4fb3-812c-a8b2546f4e85" />

<img width="1919" height="1004" alt="image" src="https://github.com/user-attachments/assets/fcbf2012-f903-491c-a0b1-36679a5c17e0" />

<img width="450" height="646" alt="image" src="https://github.com/user-attachments/assets/e99924c6-5077-4812-977c-6967a8742608" />

<img width="798" height="627" alt="image" src="https://github.com/user-attachments/assets/1345beea-97f2-4d9b-b812-cfc4ba4d6a5e" />


