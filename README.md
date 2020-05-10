# Planeta Moldova 

This is the source code for the https://planetamoldova.net website.

The site contains the creations (audio sketches, tv and radio shows, animations, music and texts) of the Romanian comedy duo "Planeta Moldova", of which yours truly represents 50%, the other half being my cousin [Mitos Micleusanu](https://micleusanu.blogspot.com/).

It's all in Romanian so unfortunately you won't be able to laugh your ass off unless you speak it. 

People have been talking about pain in their bellies, tears in their eyes, rolling on the ground and other physiological effects (often involving the sphincter) after consuming our comedy. So not being able to understand the language might be beneficial to your health.
You know the saying - luck awaits where you least expect it (I just made this up).

Anyway, you can have similar fun browsing through the source code (if you can't find a more exciting way to kill time). 

Since we wrote, directed, acted, recorded and edited most of our comedy, it was only logical (to me) to also write the website from scratch. 

No CMS or Blog software (or, horror, paid service) was up to the complicated task of serving *our art* (mainly static files). 
It had to be done from scratch and done properly, using something exotic and unnecessarily complicated.

So I chose Clojure and Clojurescript and suffered through the impossibly steep learning curve while developing this website, but somehow I got to the top. 

On the bright side, this is the kind of project that you do 'for fun', where you can disregard 'best practices', 'test driven', 'agile' and all the other horrible ideas we have to put up during our 'serious' work. 

Note that I only wrote the front-end of the site. The media is published and managed using [Mediadrop](https://github.com/mediadrop/mediadrop), but it is not exposed to the Internet; the front end reads data from the database and statically serves the media files.

While I wrote this text, my girlfriend baked an amazingly looking cake. I ended up with 22 lines of text where I'm trying hard to be funny and she created this beautiful thing... Sometimes I wonder..

Anyway, what follows are boring details of how to set up and deploy the code. Some instructions might not be copy-paste-able (the docker stuff), so you'll have to replace the account name with your own if you ever get to that point.

## Data directory

Clone the `git@github.com:codeboost/planeta-data.git` repository in the dir above current dir (../).
The tree looks like this:

  	planeta-data
  	plawww <-- you are here

Then,

	export PLANETA_MEDIADROP_DATA="../planeta-data/mediadrop"

And you can now start the application.


## Starting 


### Development

Start the app:

    $ lein figwheel app

Start the less watcher:

    $ lein less auto

Ports

The app is accessible at http://localhost:3449/

The figwheel repl is on localhost:7002
This will be the Clojure REPL (the Ring server).
To start ClojureScript REPL: `(figwheel-sidecar.repl-api/cljs-repl)`


### Production:
	
	lein uberjar
	java -jar target/planeta-crt.jar

## Docker
	lein uberjar
	docker build -t florinbraghis/planeta-crt .
	docker push florinbraghis/planeta-crt

## Deployment

You need to `gem install semver` to be able to version automatically.

Steps:

		source ~/.planeta-env
		docker login
		./deploy.sh -vm


The -vm flag means increment minor version number.
The -vp flag means increment patch version number.
If you want to bump major version, do it manually.
If not supplied, the version is not changed.

To see current version of the deployed application, use the `/version` route.

Git

`master` always contains the current version of the app. 
All changes to the app are done in external branches. 
Use the ./deploy.sh script to build, tag and deploy the production build.

# License

MIT License for all the code. 
