run: ## start full stack
	docker compose -f infra/docker-compose.yml up --build

down:
	docker compose -f infra/docker-compose.yml down -v

test:
	./mvnw -q -T1C verify
