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

Ports

The app is accessible at http://localhost:3449/

The figwheel repl is on localhost:7002
This will be the Clojure REPL (the Ring server).
To start ClojureScript REPL: `(figwheel-sidecar.repl-api/cljs-repl)`
        



