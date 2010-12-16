package org.opennms.web.controller.outage;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.element.ElementUtil;
import org.opennms.web.element.Node;
import org.opennms.web.filter.Filter;
import org.opennms.web.outage.Outage;
import org.opennms.web.outage.SortStyle;
import org.opennms.web.outage.WebOutageRepository;
import org.opennms.web.outage.filter.NodeFilter;
import org.opennms.web.outage.filter.OutageCriteria;
import org.opennms.web.outage.filter.RecentOutagesFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>NodeOutagesController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NodeOutagesController extends AbstractController implements InitializingBean {

    private String m_successView;
    private WebOutageRepository m_webOutageRepository;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Node node = ElementUtil.getNodeByParams(request, getServletContext());

        Outage[] outages = new Outage[0];

        if (node.getNodeId() > 0) {
            List<Filter> filters = new ArrayList<Filter>();

            filters.add(new NodeFilter(node.getNodeId(), getServletContext()));
            filters.add(new RecentOutagesFilter());

            OutageCriteria criteria = new OutageCriteria(filters.toArray(new Filter[0]), SortStyle.ID, null, -1, -1);
            outages = m_webOutageRepository.getMatchingOutages(criteria);
        }

        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        modelAndView.addObject("nodeId", node.getNodeId());
        modelAndView.addObject("outages", outages);
        return modelAndView;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_successView, "property successView must be set");
        Assert.notNull(m_webOutageRepository, "webOutageRepository must be set");
    }

    private String getSuccessView() {
        return m_successView;
    }

    /**
     * <p>setSuccessView</p>
     *
     * @param successView a {@link java.lang.String} object.
     */
    public void setSuccessView(String successView) {
        m_successView = successView;
    }
    
    /**
     * <p>setWebOutageRepository</p>
     *
     * @param webOutageRepository a {@link org.opennms.web.outage.WebOutageRepository} object.
     */
    public void setWebOutageRepository(WebOutageRepository webOutageRepository) {
        m_webOutageRepository = webOutageRepository;
    }

}
