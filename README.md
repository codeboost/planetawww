##Planeta Moldova 

Site-ul proiectului. 

Setup:
- Create a symbolic link to the mediadrop's `data` directory:

    $ ln -s /path/to/mediadrop-data data

- Run the database extraction tool:
TODO: automate this step
    $ node extract-media.js > plawww/public/db/results.json


Start the app:
    $ lein figwheel app

Start the less watcher:
    $ lein less auto


        



