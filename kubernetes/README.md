# Helm chart for BKVM

In order to install BKVM in an existing cluster that runs BookKeeper you can use this Helm Chart.

## Notes

This helm chart is very simple and it only starts a pod that runs BKVM service and the embedded HerdDB database.
The default configuration should work with simple clusters, without authentication on ZooKeeper services.

Usually setting metadataServiceUri is enough if you do not have authentication for ZooKeeper.

## Contributing

Feel free to give your feedback using GitHub issues or send Pull Requests with the enhancements you do locally.

## Installing the chart

Ensure that you have KUBECONFIG properly configured and than `helm` works properly.

Clone this repository and then run this command, overring properly the metadataServiceUri parameter.

```
helm install bkvm -f bkvm/values.yaml bkvm --set metadataServiceUri=zk://pulsar-zookeeper-ca:2181/ledgers
```

Wait for the BKVM server pod to be up and running

```
kubectl get pods
```

Then you can activate port-forwarding on port 4500

```
kubectl port-forward bkvm-bkvm-server 4500:4500
```

Now you can open your browser at http://localhost:4500/
