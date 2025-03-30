FROM localstack/localstack:latest

WORKDIR /script

COPY script/up-localstack.sh /script/up-localstack.sh

RUN chmod +x /script/up-localstack.sh

CMD ["sh", "-c", "/script/up-localstack.sh && tail -f /dev/null"]