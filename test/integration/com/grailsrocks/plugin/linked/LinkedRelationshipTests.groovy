package com.grailsrocks.plugin.linked

import com.grailsrocks.plugin.linked.test.*

class LinkedRelationshipTests extends GroovyTestCase {
    def gria 
    def gina
    def dgg
    def pplw
    
    def marc
    def peter
    
    static transactional = true
    
    protected void setUp() {
        super.setUp()

        assert (gria = new Book(title:'Grails in Action').save())
        assert (gina = new Book(title:'Groovy in Action').save())
        assert (dgg = new Book(title:'Definitive Guide to Grails').save())
        assert (pplw = new Book(title:'Peopleware').save())
        
        assert (marc = new User(name:'Marc').save())
        assert (peter = new User(name:'Peter').save())
    }

    protected void tearDown() {
        super.tearDown()
    }

    boolean linkExists(srcObj, destObj, nature) {
         LinkedRelationship.createCriteria().get {
            eq('destClass', destObj.class.name)
            eq('destId', destObj.ident())
            eq('srcClass', srcObj.class.name)
            eq('srcId', srcObj.ident())
            eq('nature', nature)
        }        
    }
    
    void testLinking() {
        marc.addLinkTo(gria, 'liked')
        marc.addLinkTo(gria, 'bought')
        marc.addLinkTo(gina, 'liked')
        marc.addLinkTo(pplw, 'read')

        peter.addLinkTo(gria, 'author')
        peter.addLinkTo(gina, 'bought')
        
        peter.addLinkTo(marc, 'colleague')
        
        assertEquals 7, LinkedRelationship.count()
        assertEquals 6, LinkedRelationship.countByDestClass('com.grailsrocks.plugin.linked.test.Book')
        assertEquals 4, LinkedRelationship.countBySrcId(marc.ident())
        assertEquals 3, LinkedRelationship.countBySrcId(peter.ident())

        assertTrue linkExists(marc, gria, 'liked')
        assertTrue linkExists(marc, gria, 'bought')
        assertTrue linkExists(marc, gina, 'liked')
        assertTrue linkExists(marc, pplw, 'read')

        assertTrue linkExists(peter, gria, 'author')
        assertTrue linkExists(peter, gina, 'bought')

        assertTrue linkExists(peter, marc, 'colleague')


        assertFalse linkExists(marc, peter, 'colleague')
        assertFalse linkExists(marc, peter, 'hate')
        assertFalse linkExists(marc, gria, 'author')
        assertFalse linkExists(peter, gria, 'liked')
    }

    void checkListLinksOutTo(src, dest, expectedNatures) {
        def l = src.listLinksOutTo(dest)
        assertEquals "Number of links does not match expected number of natures", l.size(), expectedNatures.size()
        expectedNatures.each { n ->
            assertNotNull l.find { link -> link.nature == n }
        }
    }
    
    void checkListLinksOut(src, expectedDestAndNature) {
        def l = src.listLinksOut()
        def totalLinks = expectedDestAndNature.inject(0) { accum, info -> accum+info.value.size() }
        println "Expected $totalLinks links"
        assertEquals "Number of links does not match expected number of links", l.size(), totalLinks
        expectedDestAndNature.each { destInfo ->
            println "Checking dest $destInfo"
            destInfo.value.each { nature ->
                println "Checking for dest with nature $nature"
                assertNotNull l.find { link -> 
                    (link.nature == nature) && 
                    (link.destClass == destInfo.key.class.name) &&
                    (link.destId == destInfo.key.ident())
                }
            }
        }
    }
    
    void checkListLinksIn(src, expectedSrcAndNature) {
        def l = src.listLinksIn()
        def totalLinks = expectedSrcAndNature.inject(0) { accum, info -> accum+info.value.size() }
        assertEquals "Number of links does not match expected number of links", l.size(), totalLinks
        expectedSrcAndNature.each { srcInfo ->
            srcInfo.value.each { nature ->
                assertNotNull l.find { link -> 
                    (link.nature == nature) && 
                    (link.srcClass == srcInfo.key.class.name) &&
                    (link.srcId == srcInfo.key.ident())
                }
            }
        }
    }
    
    void testLinkQuerying() {
        marc.addLinkTo(gria, 'liked')
        marc.addLinkTo(gria, 'bought')
        marc.addLinkTo(gina, 'liked')
        marc.addLinkTo(pplw, 'read')

        peter.addLinkTo(gria, 'author')
        peter.addLinkTo(gina, 'bought')
        
        peter.addLinkTo(marc, 'colleague')
        
        checkListLinksOutTo(marc, gria, ['liked', 'bought'])
        checkListLinksOutTo(marc, gina, ['liked'])
        checkListLinksOutTo(marc, pplw, ['read'])

        checkListLinksOutTo(peter, gria, ['author'])
        checkListLinksOutTo(peter, gina, ['bought'])

        checkListLinksOutTo(peter, marc, ['colleague'])

        checkListLinksOut(marc, [ 
            (gria):['liked', 'bought'], 
            (gina):['liked'], 
            (pplw):['read'] 
        ])
        checkListLinksOut(peter, [ 
            (gria):['author'], 
            (gina):['bought'], 
            (marc):['colleague'] 
        ])

        checkListLinksIn(gria, [ 
            (marc):['liked', 'bought'], 
            (peter):['author'] 
        ])
        checkListLinksIn(marc, [ 
            (peter):['colleague']
        ])
        checkListLinksIn(peter, [])
    }
}
