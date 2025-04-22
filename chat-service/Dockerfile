FROM node:alpine3.21

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

RUN npx prisma generate

EXPOSE 8082

CMD ["npm", "run", "dev"]