# DenotationGraph

In order to generate a new denotation graph from a set of image captions, you will need to modify `run.sh`. You will need to change `graph_name` to the name of your corpus. (If necessary, you can also change `corpus_dir` to the directory that contains your corpus directory, i.e. `/home/data/graph_corpora` if your image caption data is contained in `/home/data/graph_corpura/new_captions`. Otherwise, simply put the directory containing your caption data in `corpora`.

Your `graph_name` should match the name of the directory containing your caption data (e.g. `mpe_test_corpus`). To start graph generation, your caption directory should contain one file, `[graph_name].spell`. This file is tab-delimited, and each line contains one caption ID followed by the corresponding caption. See `corpora/mpe_test_corpus/mpe_test_corpus.spell` for an example. The graph generation process assumes that caption IDs are formatted as `image_id#caption_idx`. Denotational similarities are computed based on shared images, so this information is important to the graph similarity computations. 

The other file you will probably need is the list of images, one image ID per line, that are in the train split of your captions. Name this file `img_train.lst` (see `corpora/mpe_test_corpus/img_train.lst` for an example). The graph generation process only computes denotational similarities over the images specified in this file. If you intend to compute denotational similarities based on all of your captions, simply include all of the image IDs in this file.

## Possible issues

Make sure the memory allocated for Java (line 39 in `run.sh`) is appropriate for your machine. If you have memory issues when generating the graph, try commenting out line 39 and using lines 41-42 instead.

Also check that the number of cores allocated for parsing (line 6 in `run.sh`) is appropriate.

In some cases, especially for large corpora, the coref ("entity") step of graph preprocessing can be extremely slow. If this is the case, replace line 29 with line 30. This step should not affect the computed denotational probabilities.

## Reading the output

The preprocessed (chunked, parsed, POS-tagged) files will be located in `corpora/graph_name/`. The graph files will be located in `corpora/graph_name/graph/`. The denotational similarity files will be located in `corpora/graph_name/graph/train/` (assuming that you defined the train images). The format of these files is described in `preprocessing/corpora/notes.txt`.
