import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';
import * as fs from 'node:fs';
import * as path from 'node:path';

const apiSidebarPath = path.resolve(__dirname, 'docs/api/sidebar.ts');
const apiSidebarItems: any[] = fs.existsSync(apiSidebarPath)
    ? require('./docs/api/sidebar').default
    : [];

const sidebars: SidebarsConfig = {
    mainSidebar: [
        'index',
        {
            type: 'category',
            label: 'About',
            collapsed: false,
            items: ['about/application-architecture', 'about/ui-screenshots'],
        },
        {
            type: 'category',
            label: 'Getting Started',
            collapsed: false,
            items: [
                'getting-started/local-environment',
                'getting-started/docker-and-ports',
                'getting-started/intellij-configuration',
            ],
        },
        {
            type: 'category',
            label: 'Development Guide',
            collapsed: false,
            items: [
                'development/module-reference',
                'development/tracing-observability',
                'development/database-migrations',
            ],
        },
        ...(apiSidebarItems.length > 0
            ? [
                {
                    type: 'category' as const,
                    label: 'API Documentation',
                    link: {type: 'generated-index' as const, slug: '/api-overview'},
                    items: apiSidebarItems,
                },
            ]
            : []),
    ],
};

export default sidebars;
