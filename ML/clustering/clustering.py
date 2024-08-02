import sys
import random
import numpy as np

from vector_util import *




def kMeansClustering(k, pointData, type='plus'):
    clusters = dict()
    if type == 'plus':
        clusters = kmeansplusplusInit(k, pointData)
    else:
        for idx in range(k):
            cluster = {
                'center': (random.uniform(0, 1), random.uniform(0, 1)),
                'cards': []
            }
            clusters[idx] = cluster
    
    pre_cluster = {}
    while pre_cluster != clusters:
        pre_cluster = clusters
        clusters = assign_clusters(pointData, clusters)
        clusters = update_clusters(pointData, clusters)
        pred = pred_cluster(pointData, clusters)

    for idx, clust_id in enumerate(pred):
        clusters[clust_id]['cards'].append(idx)

    return clusters


def kmeansplusplusInit(k, pointData):
    clusters = dict()
    clusters[0] = {
        "center": random.choice(pointData)['center'],
        'cards': []
    }

    for idx in range(1, k):
        # print(f'{idx=}')
        dist = dict()

        for i in pointData:
            point = pointData[i]
            d = sys.maxsize

            for j in clusters:
                # print(f'{clusters[j]["center"]=}')
                temp_dist = calDistPoint(point['center'], clusters[j]['center'])
                d = min(d, temp_dist)
            # print(f'{d=}')
            dist[i] = d

        # TODO: find second max
        # print(f'{dist=}')
        max_dist_point = max(dist, key=dist.get)
        clusters[idx] = {
            'center': pointData[max_dist_point]['center'],
            'cards': []
        }

    return clusters


def assign_clusters(pointData, clusters):
    k = len(clusters)
    for idx in range(len(pointData)):
        dist = []
         
        curr_point = pointData[idx]
        for i in range(k):
            dis = calDistPoint(curr_point['center'], clusters[i]['center'])
            dist.append((i, dis))
        # curr_cluster = np.argmin(dist)
        curr_cluster = min(dist, key=lambda x: x[1])[0]
        clusters[curr_cluster]['cards'].append(curr_point)
    return clusters


#Implementing the M-Step
def update_clusters(pointData, clusters):
    k = len(clusters)
    for i in range(k):
        # print([points['center'] for points in clusters[i]['cards']])
        points = np.array([[points['center'][0], points['center'][1]] for points in clusters[i]['cards']])
        # print(points)
        if len(points) > 0:
            new_center = list(points.mean(axis =0))
            # print(f'{clusters[i]["center"]=}')
            clusters[i]['center'] = new_center
            # print(f'{new_center=}')
             
            clusters[i]['cards'] = []
    return clusters


def pred_cluster(X, clusters):
    k = len(clusters)
    pred = []
    for i in range(len(X)):
        dist = []
        for j in range(k):
            dist.append(calDistPoint(X[i]['center'],clusters[j]['center']))
        pred.append(np.argmin(dist))
    return pred