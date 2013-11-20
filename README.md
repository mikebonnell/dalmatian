# dalmatian

A bot for pulling the Twitter Streaming API and pushing Tweets to other services.

## Usage

The following environment variables must be set

* `APP_CONSUMER_KEY`
* `APP_CONSUMER_SECRET`
* `USER_ACCESS_TOKEN`
* `USER_ACCESS_TOKEN_SECRET`
* `TRACK` Tweets matching this keyword will be streamed
* `HIPCHAT_ROOM_ID`
* `HIPCHAT_AUTH_TOKEN`

This is meant to run on Heroku, a `Procfile` is included.

## License

Copyright © 2013 @samn

Distributed under the Eclipse Public License, the same as Clojure.
