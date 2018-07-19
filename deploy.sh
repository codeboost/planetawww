#Creates uberjar, then builds the docker container and uploads it to docker hub.

echo "Creating uberjar..."
lein uberjar
if [ $? -eq 0 ]; then
    echo "Rebuilding docker container..."
	docker build -t florinbraghis/planeta-crt .
	if [ $? -eq 0 ]; then
		echo "Pushing..."
		docker push florinbraghis/planeta-crt
	else
		echo "Error: docker build failed."
	fi
else
    echo "Error: lein uberjar failed."
fi