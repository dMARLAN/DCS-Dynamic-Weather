![](images/DCSDynamicWeatherLogo.png)
**_WORK IN PROGRESS - NOT READY FOR PUBLIC RELEASE YET..._**

Automated DCS Mission Weather editor to set DCS mission to match real world environment conditions (temperarture, clouds, visibility, time, pressure, wind) based on the METAR data obtained from the AVWX API.

Optionally outputs METAR to Discord Webhook or Google Sheets Cell for KBC's (Kneeboard Card) stored on a Google Drive to aid in mission planning or KBC creation.

- Sets DCS mission weather to match real world weather conditions.
- Weather cannot be set while the mission is still live, this application works by setting the next mission's weather and then restarting every hour to that updated weather.
  - The mission will not restart while clients are connected to the mission, effectively "freezing" weather in place until clients disconnect.
  - This means this application is not suitable for public servers that have little to no down time with no clients connected, it is designed for private servers.
- Can manually select "Clear Day" or "Clear Night" weather from DCS F10 menu.
- Can output DCS Mission METAR to Discord Webhook or Google Sheets.
- Accounts for DCS inherent QFF -> QNH conversion error.
- Automatically desanitizes `\Program Files\Eagle Dynamics\DCS World OpenBeta\Scripts\MissionScripting.lua`
  - Be aware of running other untrusted DCS scripts when `MissionScripting.lua` is desanitized.

I'm a new programmer, if you notice any bugs or have any suggestions, please let me know! Would also appreciate any GitHub stars which may help me in my job pursuit!

Donations are welcome since I'm currently self-teaching full time 😅

[![](https://www.paypalobjects.com/webstatic/mktg/logo/pp_cc_mark_37x23.jpg)](https://www.paypal.com/paypalme/CPenarsky?country.x=CA&locale.x=en_US)

## Requirements:
- 7zip (https://www.7-zip.org/)
- JDK Class File Version 61.0 or newer (Java 17)

## Installation
- Download the latest release from [GitHub]()
- Extract the contents of the zip file into the `missions` folder.
    - e.g. `C:\Users\yourname\Saved Games\DCS.openbeta\Missions\mymission`
    - `mymission` can be renamed as you desire, but will be matched in a later step.
- Create a zone in your DCS Mission and name it `StationReference` (this can be changed in `config.json`)
    - This will be the reference station where weather will be polled. For example, if you place it at Nellis, the application will retrieve the weather from Nellis.
- Acquire a [AVWX API Key](https://avwx.rest/) (Free) and paste into `config.json`
- Create the following triggers in your DCS Mission. (See [Example Mission](https://github.com/dMARLAN/DCS-Dynamic-Weather))
    - "MISSION START" -> -> "DO SCRIPT" -> Paste the Code below, and edit "mymission" to match your desired folder name.
        ```lua
        local folder = "mymission"
        DCSDynamicWeather = {}
        DCSDynamicWeather.MISSION_FOLDER = folder
        ```
    - "MISSION START" -> -> "DO SCRIPT FILE" -> Load `DCSDynamicWeatherLoader.lua`
      - This should be AFTER the `DO SCRIPT` mentioned above.

- Run your DCS Server
    - The DCS Mission invokes the application automatically.
      - `weather-output.jar` is run first, and because `weather-update.jar` has not yet run, the first run will use the weather you manually set, and the Discord/Google Output will indicate the station is "UNKN".
      - This can be avoided by running `weather-update.jar` manually once.
        - TODO: I will probably fix this before the first public release.
    - Discord Webhook and Google Sheets set up is optional.

### Discord Webhook Setup:
- Acquire your [Discord Webhook API Key](https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks) (Free) and paste into `dto.json`

### Google Sheets Setup:
- Create a [Google Cloud OAuth 2.0 Client ID](https://console.developers.google.com/) (Free)
- Paste the downloaded credentials into your `missions\mymission` folder and name the credentials `credentials.json`
- Paste your [Spreadsheet ID](https://developers.google.com/sheets/api/guides/concepts) into `config.json`
- Paste your [Spreadsheet Range](https://developers.google.com/sheets/api/guides/concepts) into `config.json`
- Execute `weather-output.jar` and authorize the Google Sheets API.
    - This _should_ save the refresh token and it can continue to be used for future executions without re-authorization.
