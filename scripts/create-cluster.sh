#!/bin/bash

# Delete any existing clusters first
minikube delete --all

# Create Minikube cluster with 3 nodes
minikube start --nodes 3 --cpus 4 --memory 4096

# Label worker nodes
kubectl label node minikube-m02 worker=true
kubectl label node minikube-m03 worker=true

echo "Cluster 'RapidRelease' created with 3 nodes (1 control plane, 2 workers)"