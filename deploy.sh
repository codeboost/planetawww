#Creates uberjar, then builds the docker container and uploads it to docker hub.

if git diff-index --quiet HEAD --; then
    echo "Repository contains no unstanged changes, that's good."
else
	echo "Repository has changes. Please commit changes before deploying."
	exit 1
fi

echo "Creating uberjar..."
lein uberjar
if [ $? -eq 0 ]; then
    echo "Rebuilding docker container..."
	docker build -t florinbraghis/planeta-crt .
	if [ $? -eq 0 ]; then
		echo "Pushing..."
		if [[ $* == *-vm ]]
		then
			echo "Incrementing minor version"
			semver inc minor
			git add '.semver'
			git commit -m 'Updated minor version.'
			git tag -a `semver tag`
		fi

		if [[ $* == *-vp ]]
		then
			echo "Incrementing patch version"
			semver inc patch
			git add '.semver'
			git commit -m 'Updated patch version.'
			git tag -a `semver tag`
		fi

		echo "Pushing `semver tag`"			
		docker push florinbraghis/planeta-crt
	else
		echo "Error: docker build failed."
	fi
else
    echo "Error: lein uberjar failed."
fi