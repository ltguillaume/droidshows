k3b trying to modernize droidshows

v 250125 restructure file layout to make project compatible with gradle/androidstudio
    done: able to compile and debug in adroidstudio
v 250412 migrate to oldest androidX 1.0.0

v 250414 implemented backup for android5ff with write-permissions+saf

Sub-Goals

> make backup/restore work again under android5 and later
> > Backup for android5ff with write-permissions+saf
* Backup with last dir or (on long press) with output-dir-picker. ??own menu/dialog with export specific settings??
* backup error handling (ie "lost dir permission" or "outdir does not exist any more")
* Method to create outfilename+extension (i.e. instead of name+ext+0): add yymmdd suffix
* Restore for android5ff with write-permissions+saf
* ?? add thumbs to backup/restore (?? to zip ??) so to avoid reload data from internet 
* import/export as (?zipped?) csv, xmp. db. Export only md, html
* refactor backup/restore logic to util modul

* get rid of deprecated ListActivity
  * by replacing with AppCompatActivity + RecyclerView