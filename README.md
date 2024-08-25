# BingoMC Partner Sync Plugin
A Velocity plugin allowing server admins to easily integrate the BingoMC Partner API.
Partner API docs: https://jensderouter.notion.site/Partner-API-ed6f2cd44773495d917f8ed192ae79cb?pvs=4

The plugin features an administrative command to create/delete servers.
It also automatically syncs those servers to the Velocity proxy.
That means you can easily access them using standard Velocity commands:
`/server bingo_<server_id>`.

## Usage
The plugin features a single command: `/bingomc`.
It has two subcommands:
- `/bingomc create` - create a new Bingo server (errors when you've reached your quota)
- `/bingomc delete <server_id>` - schedules a server for deletion

## Config
The config looks like this. Change api-key to your actual api key.
```yaml
config-version: 1

api-key: placeholder
api-url: https://privategame.bingomc.net/api/partner
```

Happy bingo! 😄