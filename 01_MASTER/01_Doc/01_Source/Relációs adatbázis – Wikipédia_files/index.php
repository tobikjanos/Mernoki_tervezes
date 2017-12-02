/**
 * Site-wide configurations and start of the ImageAnnotator gadget. Split into a
 * separate file for three reasons:
 * 1. It separates the configuration from the core code, while still
 * 2. making it impossible for someone else (e.g. a malicious user) to override
 *    these defaults, and
 * 3. makes configuration changes available quickly: clients cache this file for four hours.
 * 
 * Author: [[User:Lupo]], September 2009
 * License: Quadruple licensed GFDL, GPL, LGPL and Creative Commons Attribution 3.0 (CC-BY-3.0)
 * 
 * Choose whichever license of these you like best :-)
 * 
 * See https://commons.wikimedia.org/wiki/Help:Gadget-ImageAnnotator for documentation.
 */

(function () {
	/**
	 * Global settings. Edit these to configure ImageAnnotator for your Wiki. Note: these configurations
	 * are here to prevent them to be overwritten by a user in his or her user scripts. BE EXTRA CAREFUL
	 * IF YOU CHANGE THESE SETTINGS WHEN ImageAnnotator IS ALREADY DEPLOYED! Syntax or other errors here
	 * may break ImageAnnotator for everyone!
	 */
	mw.loader.using('mediawiki.util');
	var config = {
		// By default, ImageAnnotator is enabled in all namespaces (except "Special", -1) for everyone,
		// except on the project's main page.
		// Here, you can define a list of namespaces where it is additionally disabled.
		viewingEnabled : function () {
			var action = mw.config.get('wgAction');
			return mw.config.get('wgNamespaceNumber') >= 0
				&& !mw.config.get('wgIsMainPage')
				&& (action == 'view' || action == 'purge' || action == 'submit');
		},
		// For instance, to disable ImageAnnotator on all talk pages, replace the function body above by
		//     return (wgNamespaceNumber & 1) == 0;
		// Or, to disable it in the category namespace and on article talk pages, you could use
		//     return (wgNamespaceNumber != 14) && (wgNamespaceNumber != 1);
		// To enable viewing only on file description pages and on pages in the project namespace:
		//     return (wgNamespaceNumber == 6) || (wgNamespaceNumber == 4);
		// To enable viewing only for logged-in users, use
		//     return wgUserName !== null;
		// To switch off viewing of notes on the project's main page, use
		//     return !wgIsMainPage;

		// By default, editing is enabled for anyone on the file description page or the page that contains
		// the substitution of template ImageWithNotes. Here, you can restrict editing even more, for
		// instance by allowing only autoconfirmed users to edit notes through ImageAnnotator. Note that
		// editing is only allowed if viewing is also allowed.
		editingEnabled : function ()
		{
			var action = mw.config.get('wgAction');
			var nsNum  = mw.config.get('wgNamespaceNumber');
			var pageView = (action == 'view' || action == 'purge')
				&& location.href.search(/[?&](diff|oldid)=/) < 0;
			if (!pageView) return false;
			if (   (nsNum == 2 || nsNum == 3)
				&& mw.config.get('wgUserName') && mw.config.get('wgTitle').replace(/ /g, '_').indexOf(mw.config.get('wgUserName').replace (/ /g, '_')) === 0
			) {
				// Allow anyone to edit notes in their own user space (sandboxes!)
				return true;
			}
			// Otherwise restrict editing of notes to autoconfirmed users.
			return (mw.config.get('wgUserGroups').join (' ') + ' ').indexOf ('confirmed ') >= 0; // Confirmed and autoconfirmed
		},
		// To allow only autoconfirmed users to edit, use
		//     return (' ' + wgUserGroups.join (' ') + ' ').indexOf (' autoconfirmed ') >= 0;
		// The following example restricts editing on file description pages to autoconfirmed users,
		// and otherwise allows edits only on subpages in the project namespace (for instance, featured
		// image nominations...), but allows editing there for anyone.
		//     return (   (   wgNamespaceNumber == 6
		//                 && (' ' + wgUserGroups.join(' ') + ' ').indexOf(' autoconfirmed ') >= 0
		//                )
		//             || (wgNamespaceNumber == 4 && wgPageName.indexOf('/') > 0)
		//            );
		// Note that wgUserName is null for IPs.

		// If editing is allowed at all, may the user remove notes through the ImageAnnotator interface?
		// (Note that notes can be removed anyway using a normal edit to the page.)
		mayDelete: function () {
			return true;
		},

		// If the user may delete notes, may he or she delete with an empty deletion reason?
		emptyDeletionReasonAllowed: function () {
			var groups = ' ' + mw.config.get('wgUserGroups').join(' ') + ' ';
			return groups.indexOf(' sysop ') >= 0 || groups.indexOf(' rollbacker ') >= 0;
		},

		// If the user may delete, may he or she bypass the prompt for a deletion reason by setting
		// var ImageAnnotator_noDeletionPrompt = true;
		// in his or her user scripts?
		mayBypassDeletionPrompt : function ()
		{
			return (' ' + mw.config.get('wgUserGroups').join(' ') + ' ').indexOf(' sysop ') >= 0;
		},

		// If viewing is enabled at all, you can specify here whether viewing notes on thumbnails (e.g.,
		// in articles) is switched on. Logged-in users can augment this by disabling viewing notes on
		// thumbnails on a per-namespace basis using the global variable ImageAnnotator_no_thumbs.
		thumbsEnabled : function ()
		{
			return true;
		},
		// For instance, to switch off viewing of notes on thumbnails for IPs in article space, you'd use
		//     return !(namespaceNumber == 0 && wgUserName === null);
		
		// If viewing is enabled at all, you can define whether viewing notes on non-thumbnail images is
		// switched on. Logged-in users can augment this by disabling viewing notes on non-thumbnails
		// on a per-namespace basis using the global variable ImageAnnotator_no_images.
		generalImagesEnabled: function () {
			return true;
		},
		
		// If thumbs or general images are enabled, you can define whether this shall apply only to local
		// images (return false) or also to images that reside at the shared repository (the Commons). In
		// the 'File:' namespace, displaying notes on shared images is always enabled. (Provided viewing
		// notes is enabled at all there. If you've disabled viewing notes in all namespaces including
		// the 'File:' namespace for non-logged-in users, they won't see notes on images from the Commons
		// either, even if you enable it here.)
		sharedImagesEnabled: function () {
			return true;
		},

		// If thumbs or general images are enabled, you can define here whether you want to allow the
		// script to  display the notes or just a little indicator (an icon in the upper left--or right
		// on rtl wikis--corner of the image). The parameters given are
		//   name         string
		//     the name of the image, starting with "File:"
		//   is_local     boolean
		//     true if the image is local, false if it is from the shared repository
		//   thumb        object {width: integer, height: integer}
		//     Size of the displayed image in the article, in pixels
		//   full_img     object {width: integer, height: integer}
		//     Size of the full image as uploaded, in pixels
		//   nof_notes    integer 
		//     Number of notes on the image
		//   is_thumbnail boolean
		//     true if the image is a thumbnail, false otherwise
		inlineImageUsesIndicator : function (name, is_local, thumb, full_img, nof_notes, is_thumbnail)
		{
			// !!!!!! Local hack, see: https://hu.wikipedia.org/w/index.php?title=Szerkesztővita:BáthoryPéter&diff=14291077
			return true;

			/*
			// Of course you could also use wgNamespace or any other of the wg-globals here.
			return (is_thumbnail && !is_local)
				|| (( thumb.width < 250 && thumb.height < 250
					&& (thumb.width < full_img.width || thumb.height < full_img.height)
				)
					? nof_notes > 10 : false
				);
			// This default displays only an indicator icon for non-local thumbnails,
			// and for small images that are scaled down, but have many notes
			*/
		},

		// If notes are displayed on an image included in an article, ImageAnnotator normally adds a
		// caption indicating the presence of notes. If you want to suppress this for all images included
		// in articles, return false. To suppress the caption only for thumbnails, but not for otherwise
		// included images, return !is_thumbnail. To suppress the caption for all images but thumbnails,
		// return is_thumbnail. The parameters are the same as for the function inlineImageUsesIndicator
		// above.
		displayCaptionInArticles : function (name, is_local, thumb, full_img, nof_notes, is_thumbnail)
		{
			return true;
		},

		// Different wikis may have different image setups. For the Wikimedia projects, the image
		// servers are set up to generate missing thumbnails on the fly, so we can just construct
		// a valid thumbnail url to get a thumbnail, even if there isn't one of that size yet.
		// Return true if your wiki has a similar setup. Otherwise, return false.
		thumbnailsGeneratedAutomatically : function ()
		{
			return true;
		},

		// Determine whether an image is locally stored or comes from a central repository. For wikis
		// using the Commons as their central repository, this should not need changing.
		imageIsFromSharedRepository : function (img_url)
		{
			return mw.config.get('wgServer').indexOf('/commons') < 0 && img_url.indexOf('/commons') >= 0;
		},

		// Return the URL of the API at the shared file repository. Again, for wikis using the Commons
		// as their central repository, this should not need changing. If your wiki is accessible through
		// https, it's a good idea to also make the shared repository accessible through https and return
		// that secure URL here to avoid warnings about accessing a non-secure site from a secure site.
		sharedRepositoryAPI : function ()
		{
			return '//commons.wikimedia.org/w/api.php';
		},

		// Default coloring. Each note's rectangle has an outer and an inner border.
		outer_border  : '#666666', // Gray
		inner_border  : 'yellow',
		active_border : '#FFA500', // Orange, for highlighting the rectangle of the active note
		new_border    : 'red',     // For drawing rectangles
		
		// Default threshold for activating the zoom (can be overridden by users).
		zoom_threshold : 8.0,

		UI : {
			defaultLanguage : mw.config.get('wgContentLanguage'), // Don't change this!

			// Translate the texts below into the wgContentLanguage of your wiki. These are used as
			// fallbacks if the localized UI cannot be loaded from the server.
			defaults: {
				wpImageAnnotatorDelete        : 'Törlés',
				wpImageAnnotatorEdit          : 'Szerkesztés',
				wpImageAnnotatorSave          : 'Mentés',
				wpImageAnnotatorCancel        : 'Mégsem',
				wpImageAnnotatorPreview       : 'Előnézet',
				wpImageAnnotatorRevert        : 'Visszaállítás',
				wpTranslate                   : 'fordítás',
				wpImageAnnotatorAddButtonText : 'Új képjegyzet',
				wpImageAnnotatorAddSummary    :
					'[[Wikipédia:Képjegyzet|Képjegyzet hozzáadása]]$1',
				wpImageAnnotatorChangeSummary :
					'[[Wikipédia:Képjegyzet|Képjegyzet módosítása]]$1',
				wpImageAnnotatorRemoveSummary :
					'[[Wikipédia:Képjegyzet|Képjegyzet törlése]]$1',
				wpImageAnnotatorHasNotesShort : 'Ehhez a képhez képjegyzetek tartoznak.',
				wpImageAnnotatorHasNotesMsg   :
					'Ehhez a képhez képjegyzetek tartoznak. Húzd az egeret a kép fölé a megjelenítésükhöz.',
				wpImageAnnotatorEditNotesMsg  :
					'<span>\xa0A megjegyzéseket a(z)  <a href="#">x</a> oldalon tudod szerkeszteni.</span>',
				wpImageAnnotatorDrawRectMsg   :
					'Jelölj ki egy téglalapot a fenti képen (a bal egérgomb nyomva tartásával).',
				wpImageAnnotatorEditorLabel   :
					  '<span>A jegyzet szövege (tartalmazhat '
					+ '<a href="//hu.wikipedia.org/wiki/Szerkesztő:Bdamokos/Cheatsheet">wikikódot</a>)</span>',
				wpImageAnnotatorSaveError  :
					  '<span><span class="error">'
					+ 'Nem sikerült elmenti a jegyzetedet (talán szerkesztési ütközés miatt?).'
					+ '</span> '
					+ 'Másold ki a szöveget az alábbi szerkesztőablakból, és '
					+ '<a href="' + mw.util.getUrl( null, { action: 'edit' } ) + '">'
					+ 'kézi szerkesztéssel</a> illeszd be az oldalra.</span>',
				wpImageAnnotatorCopyright :
					  '<small>A jegyzet a '
					+ '<a href="//creativecommons.org/licenses/by-sa/3.0/deed.hu">CC-BY-SA-3.0</a>, valamint a '
					+ '<a href="//www.gnu.org/copyleft/fdl.html">GFDL 1.3</a> vagy későbbi változata alatt lesz közzétéve. '
					+ 'További részletekért lásd a <a href="//wikimediafoundation.org/wiki/Terms_of_Use"> '
					+ 'felhasználási feltételeinket</a>.</small>',
				wpImageAnnotatorDeleteReason :
					'Miért szeretnéd törölni a jegyzetet?',
				wpImageAnnotatorDeleteConfirm :
					'Biztosan törölni szeretnéd a jegyzetet?',
				wpImageAnnotatorHelp          : 
					  '<span><a href="hu.wikipedia.org/Wikipédia:Képjegyzet" '
					+ 'title="Segítség">Segítség</a></span>',
				// The following image should be a GIF or an 8bit indexed PNG with transparent background,
				// to make sure that even IE6 displays the transparency correctly. A normal 32bit PNG might
				// display a transparent background as white on IE6.
				wpImageAnnotatorIndicatorIcon :
					  '<span>'
					+ '<img src="//upload.wikimedia.org/wikipedia/commons/8/8a/Gtk-dialog-info-14px.png" '
					+ 'width="14" height="14" title="A fájlhoz képjegyzet tartozik" />'
					+ '</span>',
				wpImageAnnotatorCannotEditMsg :
					  '<span>Képjegyzet módosításához olyan böngészőre van szükség, ami támogatja a '
					+ '<a href="//hu.wikipedia.org/wiki/XMLHttpRequest">XMLHttpRequest</a> '
					+ 'objektumot. A te böngésződ nem támogatja ezt az objektumot, vagy nincs engedélyezve a használata '
					+ '(Internet Explorerben előfordulhat, hogy az ActiveX komponens ki van kapcsolva), '
					+ 'így nem tudod módosítani a képjegyzetet. Elnézést a kellemetlenségért.</span>'
			}
		}
	}; // End site-wide config.

	// DO NOT CHANGE ANYTHING BELOW THIS LINE

	// Start of ImageAnnotator
	if (config.viewingEnabled()) {
		jQuery(function () {
			ImageAnnotator.install(config);
		});
	}
})();