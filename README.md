# Mafia Telegram Bot
This bot is able to help the god randomize the characters between the player, facilitates interaction and many more.

Made with ♥

Enjoy!


## Requirements
- JDK 11+


## Run

**Option 1:** Build and run the project directly

1. Set an environment variable containing your `TELEGRAM_TOKEN`
2. Start up a redis server. configure via `REDIS_HOST` and `REDIS_PORT` environment variables.
   The default values are `localhost` and `6379`.
3. run `./gradlew bootRun` (or on Windows machines `gradle.bat bootRun`)

**Option2:** Use docker and docker-compose

1. Build using the command `docker-compose build`
2. Set an environment variable containing your `TELEGRAM_TOKEN`
3. run with `docker-compose run`
