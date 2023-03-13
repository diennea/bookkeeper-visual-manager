# Helm chart for BKVM

In order to install BKVM in an existing cluster that runs BookKeeper you can use this Helm Chart.

## Notes

The helm chart creates a Deployment that runs the BKVM service and the embedded HerdDB database.
The default configuration should work with simple clusters, without authentication on ZooKeeper services.

Usually setting metadataServiceUri is enough if you do not have authentication for ZooKeeper.

## Contributing

Feel free to give your feedback using GitHub issues or send Pull Requests with the enhancements you do locally.

## Installing the chart

Ensure that you have KUBECONFIG properly configured and than `helm` works properly.

```
helm repo add bkvm https://diennea.github.io/bookkeeper-visual-manager/
helm install bkvm bkvm/bkvm --set 'metadataServiceUri=zk://pulsar-zookeeper-ca:2181/ledgers'
```

Wait for the BKVM server deployment to be up and running

```
kubectl wait deployment/bkvm-server --for condition=Available=True --timeout=90s
```

Then you can activate port-forwarding on port 4500

```
kubectl port-forward deployment/bkvm-server 4500:4500
```

Now you can open your browser at [http://localhost:4500/](http://localhost:4500/)

## Docs 
Checkout the [documentation](https://diennea.github.io/bookkeeper-visual-manager/chart/) to see all the available options.
