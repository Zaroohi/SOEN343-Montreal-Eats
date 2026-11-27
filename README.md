# SOEN343- Baked To Go
# Baked To Go (SOEN 343, Phase II)

Pastry ordering platform. Phase II implements REG (registration, login) and ORD (bakeries, menus, order lifecycle) with a minimal dashboard.

## Platform requirements

### Local development (no Docker)
- JDK 21 or newer
- Node.js 18 or newer (with npm)
- Maven is not needed (the wrapper `mvnw` is included)
- macOS, Linux or Windows; ports 8080 and 5173 must be free

## Compile and run locally (without Docker)

Backend (start this first):

cd backend
./mvnw test              
./mvnw spring-boot:run   

On Windows use `mvnw.cmd`.

Frontend (in a second terminal):


go from the root to cd frontend
then run npm install
then run npm install leaflet
then finally run npm run dev              # http://localhost:5173

### Docker development
- Docker Desktop
- Ports 8080 and 5173 must be free

## Run with Docker Compose

```bash
# Build and start both services
docker-compose up --build

# Stop
docker-compose down
```

To run the comparison file for refactored code vs nonrefactored:
python docs/metrics/compare.py


## Demo accounts

| Role | ID | Password |
| Customer | c1 | pass123 |
| Bakery staff (b1) | s1 | pass456 |
| Courier | d1 | pass789 |
| Operator | o1 | admin123 |

Restart the backend to reset to the seeded state.