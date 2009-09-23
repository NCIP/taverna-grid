package org.cagrid.cql.cqlbuilder.metadata;

/**
 * This class represents association between two UML classes
 * @author Monika
 */
public class Association {

    private boolean bidirectional;
    private AssociationEdge source;
    private AssociationEdge target;

    /*@Override
    public String toString() {
    StringBuilder sb = new StringBuilder();
    String pre = "      Bidirectional: "+bidirectional+"\n      SOURCE:\n";
    sb.append(pre);
    sb.append(source.toString());
    sb.append("\n      TARGET:\n");
    sb.append(target.toString());
    return sb.toString();
    }*/

    public boolean isBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    public AssociationEdge getSource() {
        return source;
    }

    public void setSource(AssociationEdge source) {
        if(this.source!=null)
            throw new RuntimeException("why oh why???");
        this.source = source;
    }

    public AssociationEdge getTarget() {
        return target;
    }

    public void setTarget(AssociationEdge target) {        
        if(this.target!=null)
            throw new RuntimeException("why oh why???");
        this.target = target;
    }


}
