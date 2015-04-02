# DroidSeries<img src="https://github.com/ltGuillaume/DroidSeries/raw/master/icon/icon5a.png" align="right"/>
DroidSeries-Guillaume: 2015 Reboot of DroidSeries TV Show Tracker  
This fork adds a quite a bunch of features, including more show information, a menu overflow button, swiping gestures, double episode entries clean-up, faster updating, cover and fan art view, a modern launcher icon and it speeds up responsiveness significantly by (more) efficient SQL queries and some threading. Some pointers:
* Don't forget to update all shows regularly
* Long-press items for more options
* Swipe left-to-right to go back  
_In Shows Overview_:
* Tap poster for show info
* Long-press poster for IMDb
* Swipe right-to-left to mark next as seen  
_In Show Details_:
* Tap poster for full-screen view
* Click full-screen poster image for fan art
_In Show/Episode Details_:
* Tap IMDb rating to view in IMDb App when installed, or on IMDb's mobile webpage

[Screenshots & comparison with original DroidSeries](http://gallery.asymmetrics.nl/index.php?sfpg=RHJvaWRTZXJpZXMvKioxZTNiOGI1MjAwYzk0ZTE4OTVmZTRmYWIxOTBjNDAyYQ)  
[XDA Forum post (in original DroidSeries thread)](http://forum.xda-developers.com/showthread.php?p=58437755#post58437755)  
__Download APK__: from XDA forum post or [here](https://github.com/ltGuillaume/DroidSeries/tree/master/apk)

__Shows Overview__:  
\+ Showing "[aired unwatched] of [total unwatched]"  
\+ Added icon ic_menu_view for show/hide toggled  
\+ Status of show in details, and † in overview if show is not continuing  
\+ If show position changed, scroll back to show after [Mark next episode as seen] and Seasons list  
\+ Option in [About] to only update shows' last season  
\+ Context item to view show details on IMDb  
\+ Tap cover for show info, long-press for IMDb details  
\+ Swipe right-to-left to mark next episode as seen (shows confirmation toast)  
\+ Option to include specials in unwatched count  
\+ Mark next episode seen via swipe now vibrates  
\+ Undo function (until exit)  
\+ Showing middot · when all new episodes are aired  
\+ New show & episode details views  
\+ View full size poster and fan art  
\* Sorting shows by first unseen episode  
\* Clarified toggle and sort options  
\* Not showing "null" entries from DB  
\* Posters now fill row height, aspect ratios independent of screen's  
  
__Seasons/Episodes list__:  
\+ Showing "[aired] of [season episodes]"  
\+ Aired date in episodes list  
\+ Click on episode title for details, on checkmark to change seen state  
\* Big performance improvement for entirely rewritten Seasons activity: is now almost instant  
  
__Add show__:  
\+ Icon resized rate_star_med_on_holo_dark for added shows  
\+ Icon ic_menu_add for new shows  
\* Fixed search not working after initial search  
\* Centered icons vertically in search results  
\- No large-size posters are saved to save space in /data/data/  
  
__Update__:  
\* Prevent double episodes  
  
__Other__:  
\+ [Clean database] in About to remove double episodes post hoc  
\+ Menu (overflow) button should show up in Android 3.0+  
\+ Dutch translation  
\+ Translucent background  
\+ Swipe left-to-right acts as back button  
\+ Animations that help understand the app's structure  
\* Date/time format according to locale  
\* Big performance improvement for Overview activity: is almost instant (values are kept up-to-date in series table)  
\* Some progress dialogs cancelable  
\* Update of all shows continues when navigating away from DroidSeries  
\* Screen off/rotating screen/navigating away from app during update kinda OK now  
\* [Exit] removes app from memory  
\* Fixed UI glitches/styles  
\* Code clean-up (all layouts revised)  
\- Removed useless dependencies  
\- Removed crappy features  
  
©2010 [Carlos Limpinho, Paulo Cabido](http://code.google.com/p/droidseries) under GPL  
Modified by [Mikael Berthe](http://gitorious.org/droidseries/mckaels-droidseries)  
©2014-2015 Guillaume [previously on Gitorious](http://gitorious.org/droidseries/droidseries-guillaume)  
New icon file is a mix of work by [Thrasos Varnava](http://iconeasy.com/icon/tv-shows-2-icon) and [Taenggo](http://wallalay.com/wallpapers-for-android-67-177682-desktop-background.html)