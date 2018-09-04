corpus_dir = '/home/aylai2/test_corpora/'
corpus = 'mpe_test_corpus'

node_caps = {}
with open(corpus_dir + corpus + '/graph/node-cap.map', 'r') as in_file:
    for line in in_file:
        tokens = line.strip().split("\t")
        node_caps[tokens[0]] = set()
        for tok in tokens[1:]:
            node_caps[tokens[0]].add(tok)

cap_nodes = {}
print('cap->node')
with open(corpus_dir + corpus + '/graph/cap-node.map', 'r') as in_file:
    for line in in_file:
        tokens = line.strip().split("\t")
        cap_id = tokens[0]
        cap_nodes[cap_id] = set()
        nodes = tokens[1:]
        for node in nodes:
            cap_nodes[cap_id].add(node)
            if cap_id not in node_caps[node]:
                print(cap_id, node)

print('node->cap')
for node in node_caps:
    for cap_id in node_caps[node]:
        if node not in cap_nodes[cap_id]:
            print(cap_id, node)
