$(function() {
    $(".source").each(function() {
        var sourceDiv = $(this);
        var slug = sourceDiv.data("source-slug");
        $.ajax(jsRoutes.controllers.BuildingController.subInfo(placeRef, slug))
            .done(populateSource(sourceDiv, true))
            .fail(function(jqXHR, textStatus, errorThrown) {
                data = {
                    result: 'error',
                    blurb: 'Unable to get this data at this time.'
                };
                populateSource(sourceDiv, false)(data);
            })
    });
});

function populateSource(sourceDiv, hasDetails) {
    return function(data) {
        var sResult = data['result'];
        sourceDiv.addClass("source-"+sResult);
        sourceDiv.removeClass("source-working");
        if (sResult === "positive") {
            sourceDiv.addClass("panel-success");
            sourceDiv.removeClass("panel-default");
        } else if (sResult === "neutral") {
            sourceDiv.addClass("panel-info");
            sourceDiv.removeClass("panel-default");
        } else if (sResult === "negative") {
            sourceDiv.addClass("panel-danger");
            sourceDiv.removeClass("panel-default");
        }

        sourceDiv.find(".blurbtext").html(data['blurb']);

        if (hasDetails) {

            sourceDiv.find(".panel-footer").html(data['details']);
            var detailTable = sourceDiv.find(".panel-footer > table");
            if (detailTable.length > 0) {
                // move the table out of the footer so it gets special nifty formatting
                detailTable.appendTo(sourceDiv);
                sourceDiv.find(".panel-footer").remove();

                // and make sure to give it the CSS styles the footer used to have
                detailTable.addClass("source-footer");
                detailTable.addClass("collapse");
            }

            sourceDiv.find(".more-details").removeClass("hidden");
            sourceDiv.find(".panel-details a").click(function(evt) {
                evt.preventDefault();
                sourceDiv.find(".source-footer").collapse('toggle');
                sourceDiv.find(".less-details").toggleClass("hidden");
                sourceDiv.find(".more-details").toggleClass("hidden");
            });
        }
    }
}