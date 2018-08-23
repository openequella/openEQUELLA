#!/usr/bin/env bash
sed -i 's/OEQ_ADMIN_DOMAIN/'"$OEQ_ADMIN_DOMAIN"'/g' /home/equella/equella/learningedge-config/mandatory-config.properties
sed -i 's/OEQ_ADMIN_PORT/'"$OEQ_ADMIN_PORT"'/g' /home/equella/equella/learningedge-config/mandatory-config.properties
sed -i 's@OEQ_ADMIN_SUFFIX@'"$OEQ_ADMIN_SUFFIX"'@g' /home/equella/equella/learningedge-config/mandatory-config.properties
sed -i 's/OEQ_DB_HOST/'"$OEQ_DB_HOST"'/g' /home/equella/equella/learningedge-config/hibernate.properties
sed -i 's/OEQ_DB_PORT/'"$OEQ_DB_PORT"'/g' /home/equella/equella/learningedge-config/hibernate.properties
sed -i 's/OEQ_DB_NAME/'"$OEQ_DB_NAME"'/g' /home/equella/equella/learningedge-config/hibernate.properties
sed -i 's/OEQ_DB_USERNAME/'"$OEQ_DB_USERNAME"'/g' /home/equella/equella/learningedge-config/hibernate.properties
sed -i 's/OEQ_DB_PASSWORD/'"$OEQ_DB_PASSWORD"'/g' /home/equella/equella/learningedge-config/hibernate.properties
