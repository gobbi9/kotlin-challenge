// @ts-check
// Docusaurus configuration for the coupon-kotlin coding challenge.

import {themes as prismThemes} from 'prism-react-renderer';
import simplePlantUML from '@akebifiky/remark-simple-plantuml';

/**
 * Service base URL used by the OpenAPI playground "Try it" feature.
 * Override at build time with: API_BASE_URL=https://my-host docusaurus build
 */
const apiBaseUrl = process.env.API_BASE_URL || 'http://localhost:8082';

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'coding challenge',
    tagline: 'Technical documentation for the coupon-kotlin service',
    favicon: 'img/coupon-favicon.svg',

    url: 'https://gobbi9.github.io',
    baseUrl: '/kotlin-challenge/',

    organizationName: 'gobbi9',
    projectName: 'kotlin-challenge',
    deploymentBranch: 'gh-pages',
    // @ts-ignore
    trailingSlash: false,

    onBrokenLinks: 'warn',

    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },

    markdown: {
        mermaid: true,
        format: 'detect',
        hooks: {
            onBrokenMarkdownLinks: 'warn',
        },
    },

    themes: ['@docusaurus/theme-mermaid', 'docusaurus-theme-openapi-docs'],

    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    routeBasePath: '/',
                    sidebarPath: './sidebars.ts',
                    editUrl:
                        'https://github.com/gobbi9/kotlin-challenge/edit/main/documentation/',
                    docItemComponent: '@theme/ApiItem',
                    remarkPlugins: [
                        [simplePlantUML, {baseUrl: 'https://www.plantuml.com/plantuml/svg'}]
                    ],
                },
                blog: false,
                theme: {
                    customCss: './src/css/custom.css',
                },
            }),
        ],
    ],

    plugins: [
        [
            "@easyops-cn/docusaurus-search-local",
            {
                hashed: true,
                indexBlog: false,
                docsRouteBasePath: "/",
            },
        ],
        [
            'docusaurus-plugin-openapi-docs',
            {
                id: 'openapi',
                docsPluginId: 'classic',
                config: {
                    coupon: {
                        specPath: '../service/src/main/resources/openapi/documentation.yaml',
                        outputDir: 'docs/api',
                        sidebarOptions: {
                            groupPathsBy: 'tag',
                        },
                        baseUrl: apiBaseUrl,
                    },
                },
            },
        ],
    ],

    themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            colorMode: {
                defaultMode: 'dark',
                respectPrefersColorScheme: true,
            },
            image: 'img/social-card.png',
            navbar: {
                title: 'coding challenge',
                logo: {
                    alt: 'Coupon coding challenge',
                    src: 'img/coupon-logo.svg',
                },
                items: [
                    {
                        type: 'docSidebar',
                        sidebarId: 'mainSidebar',
                        position: 'left',
                        label: 'Docs',
                    },
                    {
                        href: 'https://github.com/gobbi9/kotlin-challenge',
                        label: 'GitHub',
                        position: 'right',
                    },
                ],
            },
            footer: {
                style: 'dark',
                links: [
                    {
                        title: 'Docs',
                        items: [
                            {label: 'Application Architecture', to: '/about/application-architecture'},
                            {label: 'Local Environment', to: '/getting-started/local-environment'},
                            {label: 'API Documentation', to: '/api-overview'},
                        ],
                    },
                    {
                        title: 'Project',
                        items: [
                            {
                                label: 'GitHub',
                                href: 'https://github.com/gobbi9/kotlin-challenge',
                            },
                        ],
                    },
                ],
                copyright: `coupon-kotlin coding challenge — built with Docusaurus.`,
            },
            prism: {
                theme: prismThemes.github,
                darkTheme: prismThemes.dracula,
                additionalLanguages: ['java', 'kotlin', 'bash', 'yaml', 'docker', 'json', 'properties', 'groovy'],
            },
            mermaid: {
                theme: {light: 'neutral', dark: 'dark'},
            },
            docs: {
                sidebar: {
                    hideable: true,
                    autoCollapseCategories: false,
                },
            },
        }),
};

export default config;
