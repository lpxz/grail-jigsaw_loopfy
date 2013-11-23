package org.w3c.www.protocol.http;

import java.util.StringTokenizer;
import java.util.Vector;
import java.net.URL;

class ScopeNode {

    private static final int FILTER_INIT_SIZE = 2;

    private static final int CHILD_INIT_SIZE = 4;

    String key = null;

    RequestFilter filters[] = null;

    boolean inex[] = null;

    ScopeNode child[] = null;

    /**
     * Trigger the sync method of filters set on this node, and recurse.
     */
    synchronized void sync() {
        if (filters != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(398);
for (int i = 0; i < filters.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(398);
{
                RequestFilter f = filters[i];
                if (f == null) continue;
                f.sync();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(398);

        }
        if (child != null) {
            edu.hkust.clap.monitor.Monitor.loopBegin(399);
for (int i = 0; i < child.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(399);
{
                ScopeNode c = child[i];
                if (c == null) continue;
                c.sync();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(399);

        }
    }

    /**
     * Resolve this scope node into the provided vector.
     * @param into The vector containing the list of filter settings.
     */
    synchronized void resolve(Vector into) {
        if (filters == null) return;
        edu.hkust.clap.monitor.Monitor.loopBegin(400);
for (int i = 0; i < filters.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(400);
{
            if (filters[i] == null) continue;
            boolean is = into.contains(filters[i]);
            if ((!inex[i]) && (is)) {
                into.removeElement(filters[i]);
            } else if (inex[i] && (!is)) {
                into.addElement(filters[i]);
            }
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(400);

    }

    synchronized void setFilter(boolean ie, RequestFilter filter) {
        int slot = -1;
        if (filters == null) {
            filters = new RequestFilter[FILTER_INIT_SIZE];
            inex = new boolean[FILTER_INIT_SIZE];
            slot = 0;
        } else {
            edu.hkust.clap.monitor.Monitor.loopBegin(401);
for (int i = 0; i < filters.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(401);
{
                if (filters[i] == null) {
                    slot = i;
                    break;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(401);

            if (slot == -1) {
                slot = filters.length;
                RequestFilter nf[] = new RequestFilter[slot << 1];
                boolean ni[] = new boolean[slot << 1];
                System.arraycopy(filters, 0, nf, 0, slot);
                System.arraycopy(inex, 0, ni, 0, slot);
                filters = nf;
                inex = ni;
            }
        }
        filters[slot] = filter;
        inex[slot] = ie;
    }

    synchronized ScopeNode lookup(String key) {
        if (child == null) return null;
        edu.hkust.clap.monitor.Monitor.loopBegin(402);
for (int i = 0; i < child.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(402);
{
            if (child[i] == null) continue;
            if (key.equals(child[i].key)) return child[i];
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(402);

        return null;
    }

    synchronized ScopeNode create(String key) {
        int slot = -1;
        if (child == null) {
            child = new ScopeNode[CHILD_INIT_SIZE];
            slot = 0;
        } else {
            edu.hkust.clap.monitor.Monitor.loopBegin(403);
for (int i = 0; i < child.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(403);
{
                if (child[i] == null) {
                    slot = i;
                    break;
                }
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(403);

            if (slot == -1) {
                slot = child.length;
                ScopeNode nc[] = new ScopeNode[slot << 1];
                System.arraycopy(child, 0, nc, 0, slot);
                child = nc;
            }
        }
        return child[slot] = new ScopeNode(key);
    }

    ScopeNode(String key) {
        this.key = key;
    }

    ScopeNode() {
    }
}

class FilterEngine {

    ScopeNode root = null;

    /**
     * Split an URL into its various parts.
     * @return An array of Strings containing the URL parts.
     */
    private String[] urlParts(URL url) {
        Vector parts = new Vector(8);
        parts.addElement(url.getProtocol());
        if ((url.getPort() == -1) || (url.getPort() == 80)) {
            parts.addElement(url.getHost());
        } else {
            parts.addElement(url.getHost() + ":" + url.getPort());
        }
        String sUrl = url.getFile();
        if (sUrl.length() == 0) {
            sUrl = "/";
        }
        StringTokenizer st = new StringTokenizer(sUrl, "/");
        edu.hkust.clap.monitor.Monitor.loopBegin(404);
while (st.hasMoreTokens()) { 
edu.hkust.clap.monitor.Monitor.loopInc(404);
parts.addElement(st.nextElement());} 
edu.hkust.clap.monitor.Monitor.loopEnd(404);

        String p[] = new String[parts.size()];
        parts.copyInto(p);
        return p;
    }

    /**
     * Register this given filter in the given scope.
     * @param scope The URL prefix defining the scope of the filter.
     * @param inex Is the scope an include or an exclude scope.
     * @param filter The filter to register in the given scope.
     */
    synchronized void setFilter(URL scope, boolean ie, RequestFilter filter) {
        String parts[] = urlParts(scope);
        ScopeNode node = root;
        edu.hkust.clap.monitor.Monitor.loopBegin(405);
for (int i = 0; i < parts.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(405);
{
            ScopeNode child = node.lookup(parts[i]);
            if (child == null) child = node.create(parts[i]);
            node = child;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(405);

        node.setFilter(ie, filter);
    }

    synchronized void setFilter(RequestFilter filter) {
        root.setFilter(true, filter);
    }

    /**
     * Get a global filter of the given class.
     * @return A RequestFilter instance, or <strong>null</strong> if none
     * was found.
     */
    synchronized RequestFilter getGlobalFilter(Class cls) {
        RequestFilter filters[] = root.filters;
        edu.hkust.clap.monitor.Monitor.loopBegin(406);
for (int i = 0; i < filters.length; i++) { 
edu.hkust.clap.monitor.Monitor.loopInc(406);
{
            if (filters[i] == null) continue;
            Class fc = filters[i].getClass();
            edu.hkust.clap.monitor.Monitor.loopBegin(407);
while (fc != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(407);
{
                if (fc == cls) return filters[i];
                fc = fc.getSuperclass();
            }} 
edu.hkust.clap.monitor.Monitor.loopEnd(407);

        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(406);

        return null;
    }

    /**
     * Trigger the sync method of all installed filters.
     * This method walk through the entire filter tree, and sync all filters
     * found on the way.
     */
    synchronized void sync() {
        root.sync();
    }

    /**
     * Compute the set of filters that apply to this request.
     * This method examine the current scopes of all filters, and determine
     * the list of filters to run for the given request.
     * @return An array of filters to run for the given request, or
     * <strong>null</strong> if no filters apply.
     */
    RequestFilter[] run(Request request) {
        String parts[] = urlParts(request.getURL());
        int ipart = 0;
        ScopeNode node = root;
        Vector applies = new Vector();
        edu.hkust.clap.monitor.Monitor.loopBegin(408);
while (node != null) { 
edu.hkust.clap.monitor.Monitor.loopInc(408);
{
            node.resolve(applies);
            if (ipart < parts.length) node = node.lookup(parts[ipart++]); else break;
        }} 
edu.hkust.clap.monitor.Monitor.loopEnd(408);

        if (applies.size() == 0) return null;
        RequestFilter f[] = new RequestFilter[applies.size()];
        applies.copyInto(f);
        return f;
    }

    FilterEngine() {
        this.root = new ScopeNode("_root_");
    }
}
