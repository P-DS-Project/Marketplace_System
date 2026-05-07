@echo off
echo Compiling all Java files...
if not exist out mkdir out
powershell -Command "$files = Get-ChildItem -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName; javac -d out -cp 'lib/*' $files"
echo Compilation finished! Classes are saved in the 'out' folder.
