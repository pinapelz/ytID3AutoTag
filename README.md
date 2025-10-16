# YouTube to MP3 Auto Tagger
A GUI wrapper for yt-dlp that downloads YouTube videos to MP3 and automatically adds ID3 tags based on uploader name, title, and thumbnail

###
![image](https://user-images.githubusercontent.com/21994085/232991117-a41a33e1-f45a-4043-aa6a-e886a31d2f11.png)
###
![image](https://user-images.githubusercontent.com/21994085/233506083-01842f0d-3aa9-48fb-a2b4-497be6019e93.png)
###
![image](https://user-images.githubusercontent.com/21994085/232990936-50c96722-e7ed-4945-8971-f90e06a24fc9.png)
###
![image](https://user-images.githubusercontent.com/21994085/199175477-b2d6d5ae-d7db-455c-813f-ad1d3af7fac5.png)

# Manual
If you don't want to use the built-in GUI builder to create a "task file", you can write it manually in your file of choice

Each line will be treated as a download job (video), you may use either the full `youtube.com` or shortened  `youtu.be` link
```
URL,START_TIME-END_TIME     (HH:MM:SS  Timestamp Format)
```

ex: `https://www.youtube.com/watch?v=qvj_QSqOrBw,00:01:10-00:01:40`
- Download video `https://www.youtube.com/watch?v=qvj_QSqOrBw` from `1 min 10s` to `1 min 40s` (30s total)


# Requirements
The packages/programs below must be accessible from any path. (If you are on Windows these will need to be in your PATH environment variable)
- ffmpeg
- yt-dlp
- Java 11 or above

You must also have one of the following browsers installed and have used it to log into YouTube. This is to mitigate yt-dlp from being blocked by YouTube
```
brave, chrome, chromium, edge, firefox, opera, safari, vivaldi, whale
```

> Although this program comes with some circumvention techniques, it may still be possible for downloads to fail. When this is the case, try again later
> You may also try adding a [PO-Token](https://github.com/yt-dlp/yt-dlp/wiki/PO-Token-Guide) to make your traffic appear more legit

# Binaries
Pre-built binaries are also available. You can download it from the [Releases](https://github.com/pinapelz/ytID3AutoTag/releases) section

You can either run it like an application

For a debug log run it as `java -jar ytID3AutoTag.jar` from the terminal

# How to Update?
Download the new binary from releases, and simply overwrite the old file. Your configs will be migrated.
