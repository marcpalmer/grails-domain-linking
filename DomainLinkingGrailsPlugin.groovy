import com.grailsrocks.plugin.linked.LinkedRelationship

class LinkedGrailsPlugin {
    // the plugin version
    def version = "1.0-alpha"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/domain/com/grailsrocks/plugin/linked/test/**/*.groovy"
    ]

    // TODO Fill in these fields
    def author = "Marc Palmer"
    def authorEmail = "marc@grailsrocks.com"
    def title = "Domain Linking Plugin"
    def description = '''\\
Link any domain objects to one another to indicate user-supplied relationships e.g. "liked" or "blocked"
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/linked"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    static extractClassName(clazz) {
        // @todo add proxy support
        clazz.name
    }
    
    def doWithDynamicMethods = { ctx ->
        // We might want a convention / allow user overrides
        def filter = { artefact ->
            artefact
        }
        
        /* Example usage:
        
        
         We have several access needs.
         
         Most common:
         
         Find all incoming links of a given kind
         Find all links, in either direction
         
         Others:
         
         Find all outgoing links from object
         
         def book = Book.get(params.id)
         def user = security.userPrincipal
         
         user.linkTo(book, 'liked')
         
         def allLinks = book.links
         def allLinkedToObjects = book.linked.outward
         def allLinkingObjects = book.linked.inward

         def likesByOrTo = book.links.liked // return LinkedRelationship objects
         def likesByOrToUsers = book.links.likedByType(User) // return LinkedRelationship objects

         def liked = book.linked.out.liked // return dest objects
         def likedUsers = book.linked.out.likedByType(User)  // return dest objects

         def likers = book.linked.in.liked // return linking src objects
         def likingUsers = book.linked.in.likedByType(User) // return linking src objects

         def booksLikedByMarc = Book.linked.in.likedBy(userMarc) // return book instances with "liked" where src is userMarc
         def booksWithAuthorPeter = Book.linked.out.authoredBy(userPeter) // return book instances with "authored" where dest is userPeter

         We also need to support counting of these, and possibly counting the difference between those with a link and those without:
         
         def likingStats = Book.links.statisticsForLiked
         assert likingStats.all = 5
         assert likingStats.liked = 3


         class Book ... void liked() {
            this.linkTo(currentThreadUser, 'liked')
            this.activity('liked')
         }
         */
         
        application.domainClasses.findAll(filter).each { artefact ->
            def mc = artefact.clazz.metaClass
            
            mc.linkTo = { domainObject, nature ->
                // @todo check if it exists first!
                assert new LinkedRelationship(
                    srcClass:extractClassName(delegate.class), 
                    srcId:delegate.ident(),
                    destClass:extractClassName(domainObject.class),
                    destId:domainObject.ident(),
                    nature:nature).save()
            }
            mc.getLinks = { ->
                def obj = delegate
                LinkedRelationship.createCriteria().list {
                    or {
                        eq('destClass', extractClassName(obj.class))
                        eq('destId', obj.ident())
                    }
                }
            }
            mc.listLinksInOfNature = { nature ->
                def obj = delegate
                LinkedRelationship.createCriteria().list {
                    eq('destClass', extractClassName(obj.class))
                    eq('destId', obj.ident())
                    eq('nature', nature)
                }
            }
            mc.listLinksOutOfNature = { nature ->
                def obj = delegate
                LinkedRelationship.createCriteria().list {
                    eq('srcClass', extractClassName(obj.class))
                    eq('srcId', obj.ident())
                    eq('nature', nature)
                }
            }
            mc.listLinksIn = { ->
                def obj = delegate
                LinkedRelationship.createCriteria().list {
                    eq('destClass', extractClassName(obj.class))
                    eq('destId', obj.ident())
                }
            }
            mc.listLinksOut = { ->
                def obj = delegate
                LinkedRelationship.createCriteria().list {
                    eq('srcId', obj.ident())
                }
            }
        
            mc.listLinksInFrom = { srcObj ->
                def obj = delegate
                LinkedRelationship.createCriteria().list {
                    eq('srcClass', extractClassName(srcObj.class))
                    eq('srcId', srcObj.ident())
                    eq('destClass', extractClassName(obj.class))
                    eq('destId', obj.ident())
                }
            }
            mc.listLinksOutTo = { destObj ->
                def obj = delegate
                LinkedRelationship.createCriteria().list {
                    eq('destClass', extractClassName(destObj.class))
                    eq('destId', destObj.ident())
                    eq('srcClass', extractClassName(obj.class))
                    eq('srcId', obj.ident())
                }
            }
        
        }
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
