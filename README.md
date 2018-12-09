# Planeta Moldova 

Site-ul proiectului. 


## Data directory

Clone the `git@github.com:codeboost/planeta-data.git` repository in the dir above current dir (../).
The tree looks like this:

  	planeta-data
  	plawww <-- you are here


Then,

	export PLANETA_MEDIADROP_DATA="../planeta-data/mediadrop"


And you can now start the application.


## Starting 

Production:
	
	lein uberjar
	java -jar target/planeta-crt.jar


## Docker

	lein uberjar
	docker build -t florinbraghis/planeta-crt .
	docker push florinbraghis/planeta-crt

##Deployment

You need to `gem install semver` to be able to version automatically.

Steps:

		source ~/.planeta-env
		docker login
		./deploy.sh -vm


The -vm flag means increment minor version number.
The -vp flag means increment patch version number.
If you want to bump major version, do it manually.
If not supplied, the version is not changed.

## Development

Start the app:

    $ lein figwheel app

Start the less watcher:

    $ lein less auto

Ports

The app is accessible at http://localhost:3449/

The figwheel repl is on localhost:7002
This will be the Clojure REPL (the Ring server).
To start ClojureScript REPL: `(figwheel-sidecar.repl-api/cljs-repl)`

Git

`master` always contains the current	 version of the app. 
All changes to the app are done in external branches. 

Before deployment, tag current master with the new version number.
		git tag -a v1.0.1 -m "My version 1.0.1"

This way, it will be easy to revert the app to a specific version in case a serious issue is discovered in the newer version.

















        



