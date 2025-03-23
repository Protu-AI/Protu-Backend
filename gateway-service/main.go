package main

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"os/signal"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/go-resty/resty/v2"
)

type ServiceInstance struct {
	InstanceId string            `json:"instanceId"`
	HostName   string            `json:"hostName"`
	App        string            `json:"app"`
	IpAddr     string            `json:"ipAddr"`
	Status     string            `json:"status"`
	Port       map[string]string `json:"port"`
}

type EurekaResponse struct {
	Applications struct {
		Application []struct {
			Instance []ServiceInstance `json:"instance"`
		} `json:"application"`
	} `json:"applications"`
}

type Gateway struct {
	eurekaURL string
	services  map[string]*httputil.ReverseProxy
	ticker    *time.Ticker
	done      chan bool
}

func NewGateway(eurekaURL string) *Gateway {
	g := &Gateway{
		eurekaURL: eurekaURL,
		services:  make(map[string]*httputil.ReverseProxy),
		ticker:    time.NewTicker(30 * time.Second),
		done:      make(chan bool),
	}
	g.updateServices()
	go g.periodicUpdate()
	return g
}

func (g *Gateway) updateServices() error {
	if err := g.fetchFromEureka(); err != nil {
		g.useStaticConfiguration()
	}
	return nil
}

func (g *Gateway) fetchFromEureka() error {
	resp, err := resty.New().R().
		SetHeader("Accept", "application/json").
		Get(g.eurekaURL + "/eureka/apps")

	if err != nil {
		return err
	}

	var eurekaResp EurekaResponse
	if err := json.Unmarshal(resp.Body(), &eurekaResp); err != nil {
		return err
	}

	if len(eurekaResp.Applications.Application) > 0 {
		for _, app := range eurekaResp.Applications.Application {
			for _, instance := range app.Instance {
				if instance.Status != "UP" {
					continue
				}

				port := 0
				if portStr, ok := instance.Port["$"]; ok {
					if p, err := strconv.Atoi(portStr); err == nil {
						port = p
					} else {
						continue
					}
				}

				serviceURL := fmt.Sprintf("http://%s:%d", instance.IpAddr, port)
				target, err := url.Parse(serviceURL)
				if err != nil {
					continue
				}

				g.services[strings.ToLower(instance.App)] = httputil.NewSingleHostReverseProxy(target)
			}
		}
		return nil
	}
	return fmt.Errorf("no services found")
}

func (g *Gateway) useStaticConfiguration() {
	if userTarget, err := url.Parse("http://user-service-container:8085"); err == nil {
		g.services["user-service"] = httputil.NewSingleHostReverseProxy(userTarget)
	}
	if chatTarget, err := url.Parse("http://chat-service-container:8082"); err == nil {
		g.services["chat-service"] = httputil.NewSingleHostReverseProxy(chatTarget)
	}
	if codeExecutionTarget, err := url.Parse("http://code-execution-service-container:8086"); err == nil {
		g.services["code-execution-service"] = httputil.NewSingleHostReverseProxy(codeExecutionTarget)
	}
}

func (g *Gateway) periodicUpdate() {
	for {
		select {
		case <-g.ticker.C:
			g.updateServices()
		case <-g.done:
			g.ticker.Stop()
			return
		}
	}
}

func (g *Gateway) Shutdown() {
	g.done <- true
}

func (g *Gateway) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	path := r.URL.Path

	switch {
	case strings.HasPrefix(path, "/api/v1/auth/") || strings.HasPrefix(path, "/api/v1/users/"):
		if proxy, ok := g.services["user-service"]; ok {
			proxy.ServeHTTP(w, r)
			return
		}
	case strings.HasPrefix(path, "/api/v1/messages/") ||
		strings.HasPrefix(path, "/api/v1/chats") ||
		strings.HasPrefix(path, "/api/v1/attachments/"):
		if proxy, ok := g.services["chat-service"]; ok {
			proxy.ServeHTTP(w, r)
			return
		}
	case strings.HasPrefix(path, "/api/v1/execute"):
		if proxy, ok := g.services["code-execution-service"]; ok {
			proxy.ServeHTTP(w, r)
			return
		}
	case path == "/health":
		json.NewEncoder(w).Encode(map[string]string{"status": "OK"})
		return
	}

	w.WriteHeader(http.StatusNotFound)
	json.NewEncoder(w).Encode(map[string]interface{}{
		"error":   "Not Found",
		"message": "The requested resource does not exist.",
	})
}

func main() {
	gateway := NewGateway("http://service-discovery:8761")
	server := &http.Server{
		Addr:    ":80",
		Handler: gateway,
	}

	go func() {
		sigChan := make(chan os.Signal, 1)
		signal.Notify(sigChan, os.Interrupt, syscall.SIGTERM)
		<-sigChan

		gateway.Shutdown()
		ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()
		server.Shutdown(ctx)
	}()

	if err := server.ListenAndServe(); err != http.ErrServerClosed {
		os.Exit(1)
	}
}
