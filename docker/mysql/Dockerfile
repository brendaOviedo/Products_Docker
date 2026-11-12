FROM mysql:8.4
ENV TZ=America/Guatemala
COPY database.sql /docker-entrypoint-initdb.d/01-database.sql
CMD ["mysqld", "--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci"]
