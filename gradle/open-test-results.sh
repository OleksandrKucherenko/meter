#!/bin/bash

## http://www.gnu.org/software/bash/manual/bashref.html
## http://stackoverflow.com/questions/3124556/clean-way-to-launch-the-web-browser-from-shell-script
## http://askubuntu.com/questions/8252/how-to-launch-default-web-browser-from-the-terminal

executor=gnome-open
type gnome-open >/dev/null 2>&1 || {
    echo >&2 "gnome-open not installed.";
    executor=sensible-browser
    type sensible-browser >/dev/null 2>&1 || {
        echo >&2 "sensible-browser not installed."
        executor=x-www-browser
        type x-www-browser >/dev/null 2>&1 || {
            echo >&2 "x-www-browser not installed.";
            executor=xdg-open
            type xdg-open >/dev/null 2>&1 || {
                echo >&2 "xdg-open not installed. Aborting.";
                exit 1;
            }
        }
    }
}

URL=$1
echo "Found executor: $executor"

# run in background
exec "$executor" "$URL" &


