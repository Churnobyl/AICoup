import clustering

data = [{'center': (0.5067708492279053, 0.1476851850748062), 'cluster': -1},
{'center': (0.9361979365348816, 0.49768519401550293), 'cluster': -1},
{'center': (0.11666666716337204, 0.5273148417472839), 'cluster': -1},
{'center': (0.507031261920929, 0.14861111342906952), 'cluster': -1},
{'center': (0.39895832538604736, 0.12777778506278992), 'cluster': -1},
{'center': (0.9106770753860474, 0.657870352268219), 'cluster': -1},
{'center': (0.46041667461395264, 0.5439814925193787), 'cluster': -1},
{'center': (0.5583333373069763, 0.8949074149131775), 'cluster': -1},
{'center': (0.6174479126930237, 0.14074073731899261), 'cluster': -1},
{'center': (0.11484374850988388, 0.34259259700775146), 'cluster': -1},
{'center': (0.4572916626930237, 0.8833333253860474), 'cluster': -1},
{'center': (0.7065104246139526, 0.19074073433876038), 'cluster': -1}]

data = dict(enumerate(data))

result = clustering.kMeansClustering(5, data)

for i in result:
    print(result[i])