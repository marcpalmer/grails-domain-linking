package com.grailsrocks.plugin.linked

class LinkedRelationship {

    String srcClass
    Long srcId

    String destClass
    Long destId

    String nature
    
    Date dateCreated

    static constraints = {
        srcClass(size:1..200, nullable: false, blank: false)
        destClass(size:1..200, nullable: false, blank: false)
        srcId(nullable: false)
        destId(nullable: false)
        nature(size:1..80, nullable: false, blank: false)
    }
    
    static transients = [ 'source', 'destination' ]
    
    def getSource() {
        // impl
    }

    def getDestination() {
        // impl
    }
}
