/**
 * Package for index access specific classes.

http://lucene.472066.n3.nabble.com/DefaultIndexAccessor-tp552653p552656.html


The purpose of IndexAccessor is to coordinate Readers/Writers for a
Lucene index. Readers and Writers in Lucene are multi-threaded in that
multiple threads may use them at the same time, but they must/should be
shared and there are special rules (You cannot delete with a Reader
while a Writer is working on the index). Also, you need to refresh
Reader views every so often; this is expensive (though usually much less
so with the new reopen method).

IndexAccessor enforces the rules and controls Reader refreshing. Instead
of worrying about caching or index interaction rules, you just ask for
your Reader/Writer, use it to search or add a doc, and then return it.
The rest is taken care of for you.

This is done by keeping a cached Writer and Searcher(s) that all threads
share. References to the Searchers are counted so that after a Writer is
returned (and no other thread has a reference to the Writer),
IndexAccessor waits for all of the current Searchers to come back and
then reopens their Readers.

In this regard, you get a  similar setup to what Solr might give: from
any thread you just add docs and run searches -- you don't have to worry
about refreshing Readers or sharing Writers/Readers or one thread
deleting with a Reader while another thread tries to write with a Writer.

This setup allows you to do other cool things, like warm Searchers
before putting them into action. Thats what the code I am posting soon
is be capable of - when the Readers are reopened, search requests will
still be handled by the old Readers while the new Searchers run a sample
query with optional sort fields. This will make sure the Reader is open
and its sort caches are loaded before the first thread tries to use it.
Much faster response to applications.

You must  open a new Reader or reopen a Reader to see recently added
docs...IndexAccessor provides no real way around that. But it does make
the reopening much easier -- and your application that just wants to add
docs and search at will from multiple threads, won't have to worry about it. 

When accessing a Lucene index from multiple threads, there are a variety
of issues that you must address.

1. The Readers/Writer should be shared across threads.
2. Readers must periodically be refreshed, either by creating new
instances or using the reopen method.
3. A Reader that writes needs to be properly coordinated with a Writer
eg they cannot be used at the same time.

IndexAccessor addresses each of these issues.

How it works:

A single Writer is shared among threads that try to concurrently
retrieve and use a Writer. Once all of these threads release their
reference to the Writer, it is closed and upon the next request a new one is created.

A single Searcher for each Similarity is also shared across threads.
Upon first request, a new Searcher is created. This Searcher is then
returned upon every request. A count of every Searcher reference retrieved is
maintained.

When all references to a Writer are released, the Writer is closed and
after waiting for all of the Searchers to be returned, the Searchers are
reopened. Without warming enabled, new requests for Searchers/Readers
must wait for this reopen to complete. If warming is enabled, the old
Searchers/Readers continue handling Searcher requests until the Readers
have been reopened and any requested sort caches have been loaded.

If you ask for a writing Reader, you will not get it until a Writer is
released and vice versa.

The result is that you can freely use Writers/Readers/Searchers from any
thread without considering thread interactions. 

***

If you want to add docs, just ask for a Writer, add the docs, and
release the Writer. If you want to search, get a Searcher, search,
and release the Searcher. You don't have to worry about reopening
Readers or coordinating access.


***
You still do have to consider things like hogging the Writer/Readers -
if you don't occasionally release them, things will not stay very
interactive.
The best method is to just get the object, use it, and then return it in
a finally block. Batch load multiple docs, but if you're just randomly adding
a doc, get the Writer, add it, and then release the Writer in a finally
block. If you are batch loading a million docs and you want to be able
to see them
as they are added: get the writer and add 10,000 docs (or something),
release the Writer, get the Writer and add 10,000 docs, etc.

 * 
**/
package com.gentics.cr.lucene.indexaccessor;

