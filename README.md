\# CampusConnect 3.0 🎓



\### A Modern Campus Placement Management Platform



\*\*CampusConnect 3.0\*\* is a full-stack web application designed to streamline campus recruitment by connecting students and recruiters through a centralized placement management platform.



Built with \*\*React, Spring Boot, and PostgreSQL\*\*, CampusConnect simplifies placement drive discovery, eligibility screening, job applications, and recruitment management through a modern, responsive interface.



The project was developed using an \*\*AI-assisted development workflow with Google Antigravity\*\*, combining software engineering practices with AI-powered implementation, debugging, testing, and UI refinement.



\---



\## ✨ Key Features



\### 🎓 Student Portal

\- Student registration and secure login

\- Personalized dashboard with academic information

\- Browse and search available placement drives

\- Filter drives based on academic eligibility

\- Apply for placement opportunities

\- Track application progress and recruitment status

\- View and manage submitted applications



\### 💼 Recruiter Portal

\- Recruiter registration and authentication

\- Dedicated recruitment management dashboard

\- Create, update, and manage placement drives

\- Review student applications

\- Track applicant counts and recruitment progress

\- Update candidate application statuses



\### 🔐 Security \& Authentication

\- JWT-based authentication

\- Role-based access control for students and recruiters

\- Secure password handling

\- Protected backend API endpoints

\- Externalized database credentials and JWT configuration

\- Git-ignored local environment configuration



\### 🎨 User Experience

\- Modern SaaS-inspired dashboard

\- Responsive desktop and mobile layouts

\- Light and dark themes

\- Custom \*\*Charcoal × Mint\*\* dark-mode design

\- Interactive search and filtering

\- Clean navigation, cards, tables, and status indicators



\---



\## 🛠️ Technology Stack



| Layer | Technologies |

|---|---|

| Frontend | React, Vite, JavaScript, CSS |

| Backend | Java 17, Spring Boot, Spring Security |

| Database | PostgreSQL |

| ORM | Spring Data JPA, Hibernate |

| Authentication | JWT |

| Build Tools | Maven, npm |

| API Testing | Postman |

| Development Tools | IntelliJ IDEA, Google Antigravity |

| Version Control | Git, GitHub |



\---



\## 🏗️ Project Architecture



```text

CampusConnect/

│

├── campusconnect-frontend/

│   ├── public/

│   ├── src/

│   │   ├── api/

│   │   ├── components/

│   │   ├── context/

│   │   ├── layouts/

│   │   ├── pages/

│   │   └── routes/

│   ├── .env.example

│   └── package.json

│

├── campusconnect-backend/

│   ├── src/

│   │   ├── main/

│   │   │   ├── java/

│   │   │   │   └── com/riyabodwani/campusconnect/

│   │   │   │       ├── config/

│   │   │   │       ├── controller/

│   │   │   │       ├── dto/

│   │   │   │       ├── entity/

│   │   │   │       ├── exception/

│   │   │   │       ├── filter/

│   │   │   │       ├── repository/

│   │   │   │       ├── service/

│   │   │   │       └── util/

│   │   │   └── resources/

│   │   └── test/

│   ├── .env.example

│   ├── application-local.properties.example

│   └── pom.xml

│

├── .gitignore

└── README.md

```



\### Application Flow



```text

React Frontend

&#x20;     |

&#x20;     | HTTP / REST API

&#x20;     v

Spring Boot Backend

&#x20;     |

&#x20;     | Spring Security + JWT

&#x20;     v

Controllers → Services → Repositories

&#x20;                             |

&#x20;                             v

&#x20;                        PostgreSQL

```



\---



\## 🚀 Getting Started



\### Prerequisites



Ensure the following are installed:



\- Java 17

\- Node.js and npm

\- PostgreSQL

\- Git



\### 1. Clone the Repository



```bash

git clone https://github.com/riyaBodwani/CampusConnect.git

cd CampusConnect

```



\### 2. Configure the Database



Create a PostgreSQL database for CampusConnect.



Navigate to the backend directory:



```bash

cd campusconnect-backend

```



Copy `application-local.properties.example` to `application-local.properties` and replace the placeholders with your local configuration.



Example:



```properties

DB\_URL=jdbc:postgresql://localhost:5432/campusconnect

DB\_USERNAME=postgres

DB\_PASSWORD=your\_database\_password

JWT\_SECRET=your\_secure\_jwt\_secret

```



Use a strong, randomly generated JWT secret appropriate for the application's signing algorithm.



\*\*Important:\*\* Never commit your local configuration or real credentials to GitHub.



\### 3. Run the Backend



From the backend directory:



\*\*Windows:\*\*



```bash

.\\mvnw.cmd spring-boot:run

```



\*\*macOS / Linux:\*\*



```bash

./mvnw spring-boot:run

```



The backend runs at:



`http://localhost:8080`



\### 4. Run the Frontend



Open another terminal:



```bash

cd campusconnect-frontend

npm install

npm run dev

```



The frontend runs at:



`http://localhost:5173`



The frontend environment template is available at `campusconnect-frontend/.env.example`.



\---



\## 🧪 Testing \& Quality Assurance



CampusConnect includes automated backend tests and has undergone frontend build and browser verification.



Testing covers areas such as:



\- Student and recruiter authentication

\- Placement drive management

\- Application workflows

\- Academic eligibility handling

\- API validation and error responses

\- JWT authentication and security filters

\- Responsive UI and theme consistency



\*\*Development verification:\*\* Successful Maven compilation, frontend production build, and browser-based UI checks.



\---



\## 🤖 AI-Assisted Development



CampusConnect was developed with support from \*\*Google Antigravity\*\*, an AI-powered development environment.



AI-assisted workflows were used for:



\- Frontend component implementation and refinement

\- Backend development assistance

\- Debugging and issue resolution

\- Automated testing and verification

\- UI/UX improvements

\- Security configuration review



The project demonstrates the practical use of AI development tools alongside full-stack engineering, API design, database integration, authentication, and testing.



\---



\## 📸 Screenshots



Screenshots of the application will be added to showcase:



\- Home Page

\- Student Dashboard

\- Recruiter Dashboard

\- Login and Registration

\- Light and Dark Themes



\---



\## 🔮 Future Enhancements



Potential future improvements include:



\- Automated placement notifications

\- Resume upload and management

\- Advanced recruitment analytics

\- Interview scheduling

\- Deployment to a cloud hosting platform



These are planned enhancements and are not part of the current implementation.



\---



\## 👩‍💻 Developer



\*\*Riya Bodwani\*\*



B.Sc. Computer Science | Full-Stack Development | AI-Assisted Software Engineering



\[GitHub](https://github.com/riyaBodwani) · \[LinkedIn](https://www.linkedin.com/in/riya-bodwani-096095323/)



\---



\*CampusConnect 3.0 — Connecting Talent with Opportunity.\*
