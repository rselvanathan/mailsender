docker pull rselvanathan/mailsender:latest
isImageRunning=$(docker inspect -f {{.State.Running}} mailsender 2> /dev/null)
if [ "$isImageRunning" = "true" ]; then
	echo "Removing mailsender container"
	docker stop mailsender
	docker rm mailsender
fi
value=$(docker images -q --filter "dangling=true")
if [ "$value" = "" ]; then
	echo "No Dangling Images"
else
	echo "Removing Dangling Images"
 	docker images -q --filter "dangling=true" | xargs docker rmi
fi
docker run -d --name mailsender \
-e AWS_ACCESS_KEY_ID= \
-e AWS_SECRET_ACCESS_KEY= \
-e MAIL_HOST= \
-e MAIL_PORT= \
-e MAIL_USERNAME= \
-e MAIL_PASSWORD= \
-e AWS_SQS_QUEUE_URL= \
-e THREADPOOL_SIZE= \
-it rselvanathan/mailsender:latest