# Build stage
FROM node:23-alpine3.20 AS builder

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

RUN npx prisma generate

# Runtime stage
FROM node:23-alpine3.20

WORKDIR /app

COPY --from=builder /app/package*.json ./

RUN npm install

COPY --from=builder /app/ ./
RUN rm -rf node_modules

COPY --from=builder /app/node_modules/.prisma ./node_modules/.prisma

RUN npx prisma generate

RUN mkdir -p /app/uploads

EXPOSE 8082

CMD ["npm", "run", "dev"]