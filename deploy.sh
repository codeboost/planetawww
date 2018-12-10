#!/bin/bash

#Creates uberjar, then builds the docker container and uploads it to docker hub.

set -e

function bail-if-changes-exist {
	if git diff-index --quiet HEAD --; then
	    echo "Repository contains no unstaged changes, that's good."
	else
		echo "Repository has changes. Please commit changes before deploying."
		exit 1
	fi
}

function bail-if-not-on-branch {
	EXPECTED_BRANCH=$1
	BRANCH=$(git rev-parse --abbrev-ref HEAD)
	echo "On branch $BRANCH"
	if [[ "$BRANCH" != "$EXPECTED_BRANCH" ]]; then
	  echo "Must be on '$EXPECTED_BRANCH' git branch to deploy."
	  exit 1
	fi	
}

bail-if-not-on-branch master
bail-if-changes-exist

echo "Creating uberjar..."
lein uberjar

if [ $? -eq 0 ]; then
	if [ $? -eq 0 ]; then
		if [[ $* == *-vm ]];
		then
			echo "Incrementing minor version"
			semver inc minor
			git add '.semver'
			git commit -m 'Updated minor version.'
			git tag -a `semver tag`
		fi

		if [[ $* == *-vp ]]; then
			echo "Incrementing patch version"
			semver inc patch
			git add '.semver'
			git commit -m 'Updated patch version.'
			git tag -a `semver tag`
		fi
	
	    echo "Rebuilding docker container..."
		docker build -t florinbraghis/planeta-crt -t florinbraghis/planeta-crt:`semver tag` .

		echo "Pushing `semver tag`"			
		docker push florinbraghis/planeta-crt:`semver tag`
	else
		echo "Error: docker build failed."
	fi
else
    echo "Error: lein uberjar failed."
fi