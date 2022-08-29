![](images/DCSDynamicWeatherLogo.png)

**_WORK IN PROGRESS - NOT READY FOR PUBLIC RELEASE YET..._**

Automated DCS Mission Weather editor to set DCS mission to match real world environment conditions (temperarture, clouds, visibility, time, pressure, wind) based on the METAR data obtained from the AVWX API.

Optionally outputs METAR to Discord Webhook or Google Sheets Cell for KBC's (Kneeboard Card) stored on a Google Drive to aid in mission planning or KBC creation.

- Sets DCS mission weather to match real world weather conditions.
- Weather cannot currently be set while the mission is still live due to DCS limitations, this application works by setting the next mission's weather and then restarting every hour to that updated weather.
  - The mission will not restart while clients are connected to the mission, effectively "freezing" weather in place until clients disconnect.
  - That means this application is not suitable for public servers that have little to no down time with no clients connected.
- Can manually force clear weather or preset times from the DCS F10 menu while in an admin slot.
- Accounts for DCS inherent QFF -> QNH conversion error.
- Automatically desanitizes `\Program Files\Eagle Dynamics\DCS World OpenBeta\Scripts\MissionScripting.lua`
  - Be aware of running other untrusted DCS scripts when `MissionScripting.lua` is desanitized.

I'm a new programmer, if you notice any bugs or have any suggestions, please let me know! Would also appreciate any GitHub stars which may help me in my job pursuit!

Donations are welcome :)

[![](https://www.paypalobjects.com/webstatic/mktg/logo/pp_cc_mark_37x23.jpg)](https://www.paypal.com/paypalme/CPenarsky?country.x=CA&locale.x=en_US)

## Requirements:
- [7zip](https://www.7-zip.org/)
- [Java 17](https://www.oracle.com/java/technologies/downloads/#jdk17-windows) (JDK Class File Version 61.0 or newer)
- [Dedicated DCS Server](https://www.digitalcombatsimulator.com/en/downloads/world/server_beta/)

## Installation
#### Initial Set-up
1) Download the latest release from [GitHub]()
2) Extract the contents of the zip file into the `missions` folder.
    - e.g. `C:\Users\yourname\Saved Games\DCS.openbeta\Missions\mymission`
    - `mymission` can be renamed as you desire (this must be matched in a later step).
3) Copy `mymission\hooks\DCSDynamicWeatherHook.lua` to your Saved Games Hooks folder.
    - e.g. `C:\Users\yourname\Saved Games\DCS.openbeta\Hooks\DCSDynamicWeatherHook.lua`
4) Acquire an [AVWX API Key](https://account.avwx.rest/getting-started) (Free) and paste into `mymission\secrets\avwx.json` inside the `avwx_key` value, replacing "YOUR_API_KEY".
   ```json
    {
        "avwx_key": "a3jd923ns983fk30TeWWFGjaf329aCutFj2Ask4Js31"
    }
    ```

#### Miz Set-up
5) Create a zone in your DCS Mission using the DCS Mission Editor and name it `StationReference`.
    - Place the zone at the location of your station.
    - This will be the reference station where weather will be polled. For example, if you place it at Nellis, the application will retrieve the weather from Nellis Station.
6) Create the following triggers in your DCS Mission. (See [Example Mission](https://github.com/dMARLAN/DCS-Dynamic-Weather))
      - "MISSION START" -> -> "DO SCRIPT" -> Paste the Code below, and edit "mymission" to match your desired folder name.
          ```lua
          local folder
        folder = "mymission" -- <-- Edit this
        ---- DO NOT EDIT BELOW ------------
        DCSDynamicWeather = {}
        DCSDynamicWeather.MISSION_FOLDER = folder
          ```
      - "MISSION START" -> -> "DO SCRIPT FILE" -> Load `DCSDynamicWeatherLoader.lua`
        - This should be AFTER the `DO SCRIPT` mentioned above.
      - ![](images/DCSDynamicWeatherLogo.png/DCSDynamicWeatherMissionEditor.png)
7) Inside your Mission Briefing, include `DCSDW` somewhere inside `Situation`
   - ![](images/DCSDynamicWeatherLogo.png/DCSDynamicWeatherMissionEditorSituation.png)
8) Start your DCS Server
9) Discord Webhook and Google Sheets set up is optional, see below.

### Discord Webhook Setup:
- Acquire your Discord Webhook API Key (Free) and paste into `mymission\secrets\discord.json` inside the `discord_key` value, replacing "YOUR_API_KEY".
   ```json
    {
        "discord_key": "https://discord.com/api/webhooks/012345678901234567/943c120b27fb49580766808103d3db6943c120b27fb4_951807DeFdAsd668-08103d"
    }
    ```
![](https://support.discord.com/hc/article_attachments/1500000463501/Screen_Shot_2020-12-15_at_4.41.53_PM.png)
![](https://support.discord.com/hc/article_attachments/360101553853/Screen_Shot_2020-12-15_at_4.51.38_PM.png)
![](https://support.discord.com/hc/article_attachments/1500000455142/Screen_Shot_2020-12-15_at_4.45.52_PM.png)

### Google Sheets Setup:
1) Go to [Google API Console](https://console.developers.google.com/)
2) Go to `Credentials`
3) Click `Manage service accounts` under `Service Accounts`
4) Click `Create Service Account`
5) Create a name/description as desired.
6) Grant it the `Service Account User` role
7) Click the 3-dot menu and select `Manage Keys`
8) Click `Add Key` and `Create New Key` using `JSON` format.
9) Paste into `mymission\secrets\` and name the file `googlesheets.json`
10) Configure `config.json` and include your spreadsheet_id and spreadsheet_range:
```json
    {
        "spreadsheet_id": "3bd76bf6d6f64c94b0c0a4278e7000c96bf6d6f64c94b0c",
        "spreadsheet_range": "MySheet!C69"
    }
```
- See [Google Sheets API Overview](https://developers.google.com/sheets/api/guides/concepts) for more information on finding your spreadsheet ID or range.
