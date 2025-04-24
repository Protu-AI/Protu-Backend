FROM node:latest

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

RUN npx prisma generate

EXPOSE 8082

CMD ["npm", "run", "dev"]