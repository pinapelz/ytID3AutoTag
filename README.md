# YouTube to MP3 Auto Tagger
Quick and dirty code to convert YouTube videos to MP3 and automatically adds ID3 tags based on uploader name, title, and thumbnail
![image](https://user-images.githubusercontent.com/21994085/199174510-63b92bec-1611-4e7d-a61a-1cd4c3d4ad41.png)
![image](https://user-images.githubusercontent.com/21994085/199175477-b2d6d5ae-d7db-455c-813f-ad1d3af7fac5.png)

The program was made in mind for a way to download music unavailable elsewhere and automatically add details for import to a music player application such as Spotify
# Usage
- Create 2 folders where ran (downloaded, completed)
- Create a text file (songs.txt) and list songs to download by length
- Create a text file (blacklist.txt) 
- Include both ffmpeg.exe + yt-dlp.exe where executed
- Files that are downloaded and tagged are moved to the downloaded folder

```
Downloading Parts of a video
URL,START_TIME:END_TIME     (HH:MM:SS  Timestamp Format)
To download the entire video. Only enter the URL on a new line in songs.txt

BLACKLIST:
Enter each term to be removed/replaced in the title and author on a new line in blacklist.txt in this format:
WORD_TO_BE_REPLACED:REPLACEMENT_WORD

To remove a word (no replacement):
WORD_TO_BE_REMOVED:
```
